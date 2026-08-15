package com.brainwallet.data.repository

import android.content.SharedPreferences
import android.os.SystemClock
import androidx.core.content.edit
import com.brainwallet.BrainwalletApp
import com.brainwallet.data.source.AnalyticsSource
import com.brainwallet.data.source.PeerManagerSource
import org.koin.core.annotation.Single
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Tracks the "foreground sync duration" analytics metric: accumulated wall-clock time
 * actively spent syncing (SPV peer manager mid-sync) while the app was in the
 * foreground, from wallet creation/restore until block-height progress first crosses
 * [SYNC_DURATION_THRESHOLD]. Logged once per wallet as `sync_duration_seconds` on the
 * `user_did_complete_sync` Firebase event.
 *
 * This mirrors iOS's `WalletCoordinator` foreground-sync-duration metric so the two
 * platforms produce a directly comparable number for the same experiment -- time spent
 * backgrounded or with the app closed mid-sync is deliberately excluded, since a longer
 * elapsed-wall-clock number there would just reflect how long the user left the app
 * alone, not how fast the sync itself is. A segment opens on [startSync] (or
 * [onAppForegrounded], if still mid-sync) and closes on [stopSync] (or
 * [onAppBackgrounded]).
 */
@Single
class SyncAnalyticsRepository(
    private val analyticsSource: AnalyticsSource,
    private val peerManagerSource: PeerManagerSource,
    private val prefs: SharedPreferences,
) {
    /** elapsedRealtime() start of the currently-open foreground-sync segment, if any. */
    private var segmentStartElapsedRealtime: Long? = null

    /** Whether a sync is currently believed to be in progress, i.e. between [startSync]/[stopSync]. */
    private var isSyncActive = false

    init {
        BrainwalletApp.addOnForegroundedListener(object : BrainwalletApp.OnAppForegrounded {
            override fun onForegrounded() = onAppForegrounded()
        })
        BrainwalletApp.addOnBackgroundedListener(object : BrainwalletApp.OnAppBackgrounded {
            override fun onBackgrounded() = onAppBackgrounded()
        })
    }

    /** Marks a sync as active and opens a segment if the app is currently in the foreground. */
    fun startSync() {
        isSyncActive = true
        openSegmentIfNeeded()
    }

    /** Closes the current segment (if any) and marks the sync as no longer active. */
    fun stopSync() {
        closeSegmentIfOpen()
        isSyncActive = false
    }

    /** Resumes timing, but only if a sync is actually still in progress. */
    fun onAppForegrounded() {
        if (isSyncActive) openSegmentIfNeeded()
    }

    /** Pauses timing so the ~background stretch isn't counted as foreground sync time. */
    fun onAppBackgrounded() {
        closeSegmentIfOpen()
    }

    /**
     * Call on every sync progress tick (0f..1f). No-op once the metric has already been
     * logged for this wallet -- every app launch re-syncs a few incremental blocks from
     * the persisted last-synced height, which would otherwise cross the threshold almost
     * immediately on every relaunch.
     */
    fun onProgressUpdate(progress: Float) {
        if (prefs.getBoolean(KEY_HAS_LOGGED_INITIAL_SYNC_DURATION, false)) return
        if (progress < SYNC_DURATION_THRESHOLD) return

        // Fold in the currently-open segment so the final tick that crosses the
        // threshold is counted, then mark this wallet as evaluated either way -- don't
        // keep re-checking on every subsequent progress tick.
        closeSegmentIfOpen()
        prefs.edit { putBoolean(KEY_HAS_LOGGED_INITIAL_SYNC_DURATION, true) }

        val syncDurationSeconds = prefs.getLong(KEY_ACCUMULATED_FOREGROUND_SYNC_SECONDS, 0L)

        // Discard anything past the sanity ceiling rather than logging it -- this metric
        // only accumulates active foreground time, so this should rarely trigger; it's a
        // backstop against clock skew, not the normal case.
        if (syncDurationSeconds !in 0..MAX_SYNC_DURATION_SECONDS) return

        val uuid = UUID.randomUUID().toString()
        val endTimestamp = peerManagerSource.getLastBlockTimestamp()
        val endBlockHeight = peerManagerSource.getCurrentBlockHeight()

        prefs.edit {
            putString(KEY_LAST_UUID, uuid)
            putLong(KEY_LAST_DURATION_SECONDS, syncDurationSeconds)
            putLong(KEY_LAST_END, endTimestamp)
        }

        analyticsSource.logEventWithParams(
            KEY_EVENT,
            mapOf(
                KEY_UUID to uuid,
                KEY_DURATION_SECONDS to syncDurationSeconds.toInt(),
                KEY_END_TIMESTAMP to endTimestamp,
                KEY_END_BLOCK_HEIGHT to endBlockHeight,
            )
        )
    }

    /**
     * Clears every key this repository owns. Called on wallet wipe/reset so a fresh
     * wallet gets a fresh measurement window, matching iOS's `wipeWallet()` behavior.
     */
    fun reset() {
        segmentStartElapsedRealtime = null
        isSyncActive = false
        prefs.edit {
            remove(KEY_ACCUMULATED_FOREGROUND_SYNC_SECONDS)
            remove(KEY_HAS_LOGGED_INITIAL_SYNC_DURATION)
            remove(KEY_LAST_UUID)
            remove(KEY_LAST_DURATION_SECONDS)
            remove(KEY_LAST_END)
        }
    }

    fun getLastSyncMetadata(): SyncMetadata? {
        val uuid = prefs.getString(KEY_LAST_UUID, null) ?: return null
        val durationSeconds = prefs.getLong(KEY_LAST_DURATION_SECONDS, 0L)
        val end = prefs.getLong(KEY_LAST_END, 0L)
        return SyncMetadata(uuid, durationSeconds, end)
    }

    private fun openSegmentIfNeeded() {
        if (segmentStartElapsedRealtime != null) return
        if (prefs.getBoolean(KEY_HAS_LOGGED_INITIAL_SYNC_DURATION, false)) return
        if (BrainwalletApp.activityCounter.get() <= 0) return
        segmentStartElapsedRealtime = SystemClock.elapsedRealtime()
    }

    private fun closeSegmentIfOpen() {
        val start = segmentStartElapsedRealtime ?: return
        segmentStartElapsedRealtime = null
        val elapsedSeconds = (SystemClock.elapsedRealtime() - start) / 1000L
        if (elapsedSeconds <= 0) return
        val accumulated = prefs.getLong(KEY_ACCUMULATED_FOREGROUND_SYNC_SECONDS, 0L)
        prefs.edit { putLong(KEY_ACCUMULATED_FOREGROUND_SYNC_SECONDS, accumulated + elapsedSeconds) }
    }

    data class SyncMetadata(
        val uuid: String,
        val durationSeconds: Long,
        val endTimestamp: Long
    ) {
        class Formatter(
            private val dateFormat: SimpleDateFormat = SimpleDateFormat(
                "MMMM dd, yyyy h:mm:ss a",
                Locale.getDefault()
            )
        ) {
            fun format(syncMetadata: SyncMetadata): String {
                val date = Date(syncMetadata.endTimestamp * 1000)
                val dateString = dateFormat.format(date)

                return "Duration: %d seconds\nTimestamp: %s".format(syncMetadata.durationSeconds, dateString)
            }
        }
    }

    companion object {
        private const val KEY_ACCUMULATED_FOREGROUND_SYNC_SECONDS = "accumulated_foreground_sync_seconds"
        private const val KEY_HAS_LOGGED_INITIAL_SYNC_DURATION = "has_logged_initial_sync_duration"
        private const val KEY_LAST_UUID = "last_sync_uuid"
        private const val KEY_LAST_DURATION_SECONDS = "last_sync_duration_seconds"
        private const val KEY_LAST_END = "last_sync_end_timestamp"
        private const val KEY_DURATION_SECONDS = "sync_duration_seconds"
        private const val KEY_END_TIMESTAMP = "end_timestamp"
        private const val KEY_END_BLOCK_HEIGHT = "end_block_height"
        private const val KEY_UUID = "uuid"
        private const val KEY_EVENT = "user_did_complete_sync"

        /**
         * Fraction of block-height-based progress at which sync is considered "done" for
         * this metric -- matches iOS's `kSyncDurationThreshold`. Not 1.0, since the final
         * progress tick and the actual sync-complete transition are two separate events
         * and the last tick before completion can land anywhere below full completion.
         */
        const val SYNC_DURATION_THRESHOLD = 0.98f

        /**
         * Sanity ceiling for the metric, in seconds (24h) -- matches iOS's
         * `kMaxSyncDurationSeconds`. Should rarely if ever bite, since this only
         * accumulates active foreground time; it's a backstop against clock skew or a
         * pathologically bad connection, not the normal background-time case that
         * motivated using accumulated segments over a single wall-clock diff.
         */
        const val MAX_SYNC_DURATION_SECONDS = 24 * 60 * 60L
    }
}
