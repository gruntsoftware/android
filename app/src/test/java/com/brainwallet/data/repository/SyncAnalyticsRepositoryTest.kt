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
    fun `given sync started when startSync called then start timestamp stored`() {
        every { peerManagerSource.getLastBlockTimestamp() } returns 1000L

        repository.startSync()

        val stored = prefs.getLong("current_sync_start_timestamp", -1L)
        assert(stored == 1000L) { "expected 1000L but was $stored" }
    }

    @Test
    fun `given sync started and stopped when stopSync called then duration accumulated`() {
        prefs.edit().putLong("current_sync_start_timestamp", 1000L).apply()
        prefs.edit().putLong("accumulated_sync_duration", 200L).apply()
        every { peerManagerSource.getLastBlockTimestamp() } returns 1500L

        repository.stopSync()

        val accumulated = prefs.getLong("accumulated_sync_duration", 0L)
        val startRemoved = prefs.getLong("current_sync_start_timestamp", -1L)
        assert(accumulated == 700L) { "expected 700L but was $accumulated" }
        assert(startRemoved == -1L) { "expected current_sync_start_timestamp to be removed" }
    }

    @Test
    fun `given sync never started when stopSync called then nothing stored`() {
        prefs.edit().putLong("current_sync_start_timestamp", 0L).apply()

        repository.stopSync()

        val accumulated = prefs.getLong("accumulated_sync_duration", 0L)
        assert(accumulated == 0L) { "expected no accumulation but was $accumulated" }
    }

    @Test
    fun `given accumulated duration when completeSync called then metadata persisted and event logged`() {
        prefs.edit().putLong("accumulated_sync_duration", 5000L).apply()
        every { peerManagerSource.getLastBlockTimestamp() } returns 6000L
        every { peerManagerSource.getCurrentBlockHeight() } returns 456

        mockkStatic(UUID::class)
        every { UUID.randomUUID().toString() } returns "uuid-123"

        repository.completeSync()

        val lastUuid = prefs.getString("last_sync_uuid", null)
        val lastDuration = prefs.getLong("last_sync_duration", 0L)
        val lastEnd = prefs.getLong("last_sync_end_timestamp", 0L)
        val accumulatedCleared = prefs.getLong("accumulated_sync_duration", -1L)

        assert(lastUuid == "uuid-123") { "expected uuid-123 but was $lastUuid" }
        assert(lastDuration == 5000000L) { "expected duration=5000000 but was $lastDuration" }
        assert(lastEnd == 6000L) { "expected end=6000 but was $lastEnd" }
        assert(accumulatedCleared == -1L) { "expected accumulated_sync_duration removed" }

        val paramsSlot = slot<Map<String, Any?>>()

        verify {
            analyticsSource.logEventWithParams(
                "user_did_complete_sync",
                capture(paramsSlot)
            )
        }

        val captured = paramsSlot.captured
        println("captured: $captured")

        assert(captured["uuid"] == "uuid-123") { "uuid should match" }
        assert(captured["duration_millis"] == 5000000L) { "duration should match" }
        assert(captured["end_timestamp"] == 6000L) { "end timestamp should match" }
        assert(captured["end_block_height"] == 456) { "end block height should match" }
    }

    @Test
    fun `given metadata stored when getLastSyncMetadata called then correct SyncMetadata returned`() {
        prefs.edit()
            .putString("last_sync_uuid", "uuid-999")
            .putLong("last_sync_duration", 8888L)
            .putLong("last_sync_end_timestamp", 9999L)
            .apply()

        val metadata = repository.getLastSyncMetadata()

        assert(metadata != null) { "expected metadata to be not null" }
        assert(metadata!!.uuid == "uuid-999") { "expected uuid=uuid-999 but was ${metadata.uuid}" }
        assert(metadata.durationMillis == 8888L) { "expected duration=8888 but was ${metadata.durationMillis}" }
        assert(metadata.endTimestamp == 9999L) { "expected endTimestamp=9999 but was ${metadata.endTimestamp}" }
    }

    @Test
    fun `given no metadata stored when getLastSyncMetadata called then null returned`() {
        val metadata = repository.getLastSyncMetadata()
        assert(metadata == null) { "expected metadata to be null" }
    }
}
