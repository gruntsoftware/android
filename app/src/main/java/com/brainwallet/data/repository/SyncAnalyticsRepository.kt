package com.brainwallet.data.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import com.brainwallet.data.source.AnalyticsSource
import com.brainwallet.data.source.PeerManagerSource
import org.koin.core.annotation.Single
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

@Single
class SyncAnalyticsRepository(
    private val analyticsSource: AnalyticsSource,
    private val peerManagerSource: PeerManagerSource,
    private val prefs: SharedPreferences,
) {
    fun startSync() {
        // Record the start time of the current sync segment.
        prefs.edit {
            putLong(KEY_CURRENT_SYNC_START_TIMESTAMP, peerManagerSource.getLastBlockTimestamp())
        }
    }

    fun stopSync() {
        val start = prefs.getLong(KEY_CURRENT_SYNC_START_TIMESTAMP, 0L)
        if (start != 0L) {
            val duration = peerManagerSource.getLastBlockTimestamp() - start
            val accumulated = prefs.getLong(KEY_ACCUMULATED_DURATION, 0L)
            prefs.edit {
                putLong(KEY_ACCUMULATED_DURATION, accumulated + duration)
                remove(KEY_CURRENT_SYNC_START_TIMESTAMP)
            }
        }
    }

    fun completeSync() {
        // Capture the duration of the final segment.
        stopSync()

        val totalDuration = prefs.getLong(KEY_ACCUMULATED_DURATION, 0L)
        if (totalDuration <= 8000L) return

        val endTimestamp = peerManagerSource.getLastBlockTimestamp()
        val endBlockHeight = peerManagerSource.getCurrentBlockHeight()
        val uuid = UUID.randomUUID().toString()
        val totalDurationInMillis = totalDuration * 1000

        prefs.edit {
            putString(KEY_LAST_UUID, uuid)
            putLong(KEY_LAST_DURATION, totalDurationInMillis)
            putLong(KEY_LAST_END, endTimestamp)
            remove(KEY_ACCUMULATED_DURATION)
        }

        val params = mapOf(
            KEY_UUID to uuid,
            KEY_DURATION_MILLIS to (totalDurationInMillis),
            KEY_END_TIMESTAMP to endTimestamp,
            KEY_END_BLOCK_HEIGHT to endBlockHeight
        )
        analyticsSource.logEventWithParams(KEY_EVENT, params)
    }

    fun getLastSyncMetadata(): SyncMetadata? {
        val uuid = prefs.getString(KEY_LAST_UUID, null) ?: return null
        val duration = prefs.getLong(KEY_LAST_DURATION, 0L)
        val end = prefs.getLong(KEY_LAST_END, 0L)
        return SyncMetadata(uuid, duration, end)
    }

    data class SyncMetadata(
        val uuid: String,
        val durationMillis: Long,
        val endTimestamp: Long
    ) {
        class Formatter(
            private val dateFormat: SimpleDateFormat = SimpleDateFormat(
                "MMMM dd, yyyy h:mm:ss a",
                Locale.getDefault()
            )
        ) {
            fun format(syncMetadata: SyncMetadata): String {
                val durationSeconds = syncMetadata.durationMillis / 1000.0
                val date = Date(syncMetadata.endTimestamp * 1000)
                val dateString = dateFormat.format(date)

                return "Duration: %.1f seconds\nTimestamp: %s".format(durationSeconds, dateString)
            }
        }
    }

    companion object {
        private const val KEY_CURRENT_SYNC_START_TIMESTAMP = "current_sync_start_timestamp"
        private const val KEY_ACCUMULATED_DURATION = "accumulated_sync_duration"
        private const val KEY_LAST_UUID = "last_sync_uuid"
        private const val KEY_LAST_DURATION = "last_sync_duration"
        private const val KEY_LAST_END = "last_sync_end_timestamp"
        private const val KEY_DURATION_MILLIS = "duration_millis"
        private const val KEY_END_TIMESTAMP = "end_timestamp"
        private const val KEY_END_BLOCK_HEIGHT = "end_block_height"
        private const val KEY_UUID = "uuid"
        private const val KEY_EVENT = "user_did_complete_sync"
    }
}
