package com.brainwallet.data.repository

import android.content.SharedPreferences
import com.brainwallet.data.source.AnalyticsSource
import com.brainwallet.data.source.PeerManagerSource
import com.brainwallet.util.FakeSharedPreferences
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class SyncAnalyticsRepositoryTest {
    private lateinit var prefs: SharedPreferences

    @MockK
    private lateinit var analyticsSource: AnalyticsSource

    @MockK
    private lateinit var peerManagerSource: PeerManagerSource

    private lateinit var repository: SyncAnalyticsRepository

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        prefs = FakeSharedPreferences()
        repository = SyncAnalyticsRepository(
            analyticsSource = analyticsSource,
            peerManagerSource = peerManagerSource,
            prefs = prefs
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `given sync started while backgrounded when stopSync called then no time accumulated`() {
        // Foreground segments only open while the app is in the foreground
        // (BrainwalletApp#activityCounter is 0 in a bare unit-test process), so
        // startSync/stopSync alone -- with no foreground signal -- should not accumulate
        // any duration.
        repository.startSync()
        repository.stopSync()

        val accumulated = prefs.getLong("accumulated_foreground_sync_seconds", -1L)
        assert(accumulated == -1L) { "expected no accumulation but was $accumulated" }
    }

    @Test
    fun `given app foregrounded then backgrounded outside of a sync when nothing happens then no time accumulated`() {
        repository.onAppForegrounded()
        repository.onAppBackgrounded()

        val accumulated = prefs.getLong("accumulated_foreground_sync_seconds", -1L)
        assert(accumulated == -1L) { "expected no accumulation outside of an active sync but was $accumulated" }
    }

    @Test
    fun `given progress below threshold when onProgressUpdate called then nothing logged`() {
        repository.onProgressUpdate(0.5f)

        assert(!prefs.getBoolean("has_logged_initial_sync_duration", false)) {
            "should not be marked logged below the threshold"
        }
        verify(exactly = 0) { analyticsSource.logEventWithParams(any(), any()) }
    }

    @Test
    fun `given accumulated duration when progress crosses threshold then event logged once with sync_duration_seconds`() {
        prefs.edit().putLong("accumulated_foreground_sync_seconds", 42L).apply()
        every { peerManagerSource.getLastBlockTimestamp() } returns 6000L
        every { peerManagerSource.getCurrentBlockHeight() } returns 456

        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns "uuid-123"

        repository.onProgressUpdate(0.98f)

        assert(prefs.getBoolean("has_logged_initial_sync_duration", false)) {
            "expected the one-shot flag to be set"
        }

        val paramsSlot = slot<Map<String, Any?>>()
        verify(exactly = 1) {
            analyticsSource.logEventWithParams("user_did_complete_sync", capture(paramsSlot))
        }

        val captured = paramsSlot.captured
        assert(captured["uuid"] == "uuid-123") { "uuid should match" }
        assert(
            captured["sync_duration_seconds"] == 42
        ) { "duration should match, was ${captured["sync_duration_seconds"]}" }
        assert(captured["end_timestamp"] == 6000L) { "end timestamp should match" }
        assert(captured["end_block_height"] == 456) { "end block height should match" }

        // A second crossing tick must not log again.
        repository.onProgressUpdate(0.99f)
        verify(exactly = 1) { analyticsSource.logEventWithParams(any(), any()) }
    }

    @Test
    fun `given duration past the outlier cap when progress crosses threshold then event not logged`() {
        prefs.edit().putLong("accumulated_foreground_sync_seconds", 25 * 60 * 60L).apply()

        repository.onProgressUpdate(1f)

        assert(prefs.getBoolean("has_logged_initial_sync_duration", false)) {
            "wallet should still be marked evaluated so it isn't rechecked"
        }
        verify(exactly = 0) { analyticsSource.logEventWithParams(any(), any()) }
    }

    @Test
    fun `given metric already logged when reset called then keys cleared`() {
        prefs.edit()
            .putLong("accumulated_foreground_sync_seconds", 42L)
            .putBoolean("has_logged_initial_sync_duration", true)
            .putString("last_sync_uuid", "uuid-999")
            .putLong("last_sync_duration_seconds", 42L)
            .putLong("last_sync_end_timestamp", 9999L)
            .apply()

        repository.reset()

        assert(prefs.getLong("accumulated_foreground_sync_seconds", -1L) == -1L)
        assert(!prefs.getBoolean("has_logged_initial_sync_duration", false))
        assert(repository.getLastSyncMetadata() == null)
    }

    @Test
    fun `given metadata stored when getLastSyncMetadata called then correct SyncMetadata returned`() {
        prefs.edit()
            .putString("last_sync_uuid", "uuid-999")
            .putLong("last_sync_duration_seconds", 8888L)
            .putLong("last_sync_end_timestamp", 9999L)
            .apply()

        val metadata = repository.getLastSyncMetadata()

        assert(metadata != null) { "expected metadata to be not null" }
        assert(metadata!!.uuid == "uuid-999") { "expected uuid=uuid-999 but was ${metadata.uuid}" }
        assert(metadata.durationSeconds == 8888L) { "expected duration=8888 but was ${metadata.durationSeconds}" }
        assert(metadata.endTimestamp == 9999L) { "expected endTimestamp=9999 but was ${metadata.endTimestamp}" }
    }

    @Test
    fun `given no metadata stored when getLastSyncMetadata called then null returned`() {
        val metadata = repository.getLastSyncMetadata()
        assert(metadata == null) { "expected metadata to be null" }
    }

    @Test
    fun `given sync metadata when format then return exactly formatted string with correct structure`() {
        val fixedDateFormat = SimpleDateFormat("MMMM dd, yyyy h:mm:ss a", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val formatter = SyncAnalyticsRepository.SyncMetadata.Formatter(fixedDateFormat)

        val syncMetadata = SyncAnalyticsRepository.SyncMetadata(
            uuid = "1234",
            durationSeconds = 42L,
            endTimestamp = 1633072800L
        )

        val actual = formatter.format(syncMetadata)

        val expectedDate = fixedDateFormat.format(Date(syncMetadata.endTimestamp * 1000))
        val expected = "Duration: 42 seconds\nTimestamp: $expectedDate"

        assert(actual == expected) {
            "Expected exactly '$expected' but got '$actual'"
        }
    }
}
