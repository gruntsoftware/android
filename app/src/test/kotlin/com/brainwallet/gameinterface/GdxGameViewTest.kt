package com.brainwallet.gameinterface

import com.brainwallet.tools.manager.AnalyticsManager
import com.brainwallet.ui.screens.gamehub.GameHubEvent
import com.brainwallet.ui.screens.gamehub.GameHubViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GdxGameViewTest {

    private val lenientJson = Json { ignoreUnknownKeys = true }

    @Before
    fun setUp() {
        mockkStatic(AnalyticsManager::class)
        every { AnalyticsManager.logCustomAdHocEvent(any(), any()) } returns null
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    // ── GameExitData parsing ──────────────────────────────────────────────

    @Test
    fun `given full json payload, when decoding GameExitData, then all fields are populated`() {
        val json = """
            {
                "social_network": "twitter",
                "timestamp": 1700000000,
                "total_score": 120,
                "bonus_amount": 20,
                "score_a": 40,
                "score_b": 40,
                "score_c": 40
            }
        """.trimIndent()

        val result = lenientJson.decodeFromString<GameExitData>(json)

        assertEquals("twitter", result.socialNetwork)
        assertEquals(1700000000L, result.timestamp)
        assertEquals(120, result.totalScore)
        assertEquals(20, result.bonusAmount)
        assertEquals(40, result.scoreA)
        assertEquals(40, result.scoreB)
        assertEquals(40, result.scoreC)
    }

    @Test
    fun `given json missing fields, when decoding GameExitData, then defaults are used`() {
        val result = lenientJson.decodeFromString<GameExitData>("{}")

        assertEquals("none", result.socialNetwork)
        assertEquals(0L, result.timestamp)
        assertEquals(0, result.totalScore)
        assertEquals(0, result.bonusAmount)
    }

    @Test
    fun `given json with unknown keys, when decoding GameExitData, then unknown keys are ignored`() {
        val json = """{"social_network":"instagram","some_future_field":"unused"}"""

        val result = lenientJson.decodeFromString<GameExitData>(json)

        assertEquals("instagram", result.socialNetwork)
    }

    // ── GameExitPayload parsing (bw-gdlib's wrapWithEvents wire shape) ─────

    @Test
    fun `given wrapped payload, when decoding GameExitPayload, then exitData and events are both populated`() {
        val json = """
            {
                "exitData": {"social_network": "twitter", "total_score": 100},
                "events": [
                    {"name": "how_to_play_opened", "timestampMs": 1700000000000, "params": {"source": "menu"}}
                ]
            }
        """.trimIndent()

        val result = lenientJson.decodeFromString<GameExitPayload>(json)

        assertEquals("twitter", result.exitData.socialNetwork)
        assertEquals(100, result.exitData.totalScore)
        assertEquals(1, result.events.size)
        assertEquals("how_to_play_opened", result.events.first().name)
    }

    @Test
    fun `given payload with no events key, when decoding GameExitPayload, then events defaults to empty`() {
        val json = """{"exitData": {"social_network": "none"}}"""

        val result = lenientJson.decodeFromString<GameExitPayload>(json)

        assertEquals(emptyList<GameAnalyticsEvent>(), result.events)
    }

    // ── handleGameExit dispatch logic ─────────────────────────────────────

    @Test
    fun `given twitter exit with screenshot, when handleGameExit, then dispatches OnGameExited with the screenshot`() {
        val gameHubViewModel = mockk<GameHubViewModel>(relaxed = true)
        val screenshot = byteArrayOf(1, 2, 3, 4)
        val json = """{"exitData": {"social_network":"twitter","total_score":100}}"""

        handleGameExit(json, screenshot, gameHubViewModel)

        val slot = slot<GameHubEvent>()
        verify(exactly = 1) { gameHubViewModel.onEvent(capture(slot)) }

        val captured = slot.captured as GameHubEvent.OnGameExited
        val payloadExitData = lenientJson.decodeFromString<GameExitData>(captured.jsonPayload)
        assertEquals("twitter", payloadExitData.socialNetwork)
        assertEquals(100, payloadExitData.totalScore)
        assertArrayEquals(screenshot, captured.byteArray)
    }

    @Test
    fun `given instagram exit with screenshot, when handleGameExit, then dispatches OnGameExited with the screenshot`() {
        val gameHubViewModel = mockk<GameHubViewModel>(relaxed = true)
        val screenshot = byteArrayOf(5, 6, 7)
        val json = """{"exitData": {"social_network":"instagram"}}"""

        handleGameExit(json, screenshot, gameHubViewModel)

        val slot = slot<GameHubEvent>()
        verify(exactly = 1) { gameHubViewModel.onEvent(capture(slot)) }

        val captured = slot.captured as GameHubEvent.OnGameExited
        assertArrayEquals(screenshot, captured.byteArray)
    }

    @Test
    fun `given twitter exit with null screenshot, when handleGameExit, then does not dispatch an event`() {
        val gameHubViewModel = mockk<GameHubViewModel>(relaxed = true)
        val json = """{"exitData": {"social_network":"twitter"}}"""

        handleGameExit(json, null, gameHubViewModel)

        verify(exactly = 0) { gameHubViewModel.onEvent(any()) }
    }

    @Test
    fun `given twitter exit with empty screenshot, when handleGameExit, then does not dispatch an event`() {
        val gameHubViewModel = mockk<GameHubViewModel>(relaxed = true)
        val json = """{"exitData": {"social_network":"twitter"}}"""

        handleGameExit(json, ByteArray(0), gameHubViewModel)

        verify(exactly = 0) { gameHubViewModel.onEvent(any()) }
    }

    @Test
    fun `given exit with no social network, when handleGameExit, then does not dispatch an event`() {
        val gameHubViewModel = mockk<GameHubViewModel>(relaxed = true)
        val json = """{"exitData": {"total_score":50}}"""

        handleGameExit(json, byteArrayOf(1), gameHubViewModel)

        verify(exactly = 0) { gameHubViewModel.onEvent(any()) }
    }

    @Test
    fun `given exit with unrecognised social network, when handleGameExit, then does not dispatch an event`() {
        val gameHubViewModel = mockk<GameHubViewModel>(relaxed = true)
        val json = """{"exitData": {"social_network":"facebook"}}"""

        handleGameExit(json, byteArrayOf(1), gameHubViewModel)

        verify(exactly = 0) { gameHubViewModel.onEvent(any()) }
    }

    @Test
    fun `given malformed json, when handleGameExit, then does not throw and does not dispatch an event`() {
        val gameHubViewModel = mockk<GameHubViewModel>(relaxed = true)

        handleGameExit("not valid json", byteArrayOf(1), gameHubViewModel)

        verify(exactly = 0) { gameHubViewModel.onEvent(any()) }
    }

    // ── Event forwarding to Firebase (AnalyticsManager) ─────────────────────

    @Test
    fun `given payload with events, when handleGameExit, then each event is forwarded to AnalyticsManager`() {
        val gameHubViewModel = mockk<GameHubViewModel>(relaxed = true)
        val json = """
            {
                "exitData": {"social_network": "none"},
                "events": [
                    {"name": "how_to_play_opened", "timestampMs": 1, "params": {}},
                    {"name": "user_did_post_x", "timestampMs": 2, "params": {"score": 100}}
                ]
            }
        """.trimIndent()

        handleGameExit(json, null, gameHubViewModel)

        verify(exactly = 1) { AnalyticsManager.logCustomAdHocEvent("how_to_play_opened", any()) }
        verify(exactly = 1) { AnalyticsManager.logCustomAdHocEvent("user_did_post_x", any()) }
    }

    @Test
    fun `given payload with no events key, when handleGameExit, then AnalyticsManager is not called`() {
        val gameHubViewModel = mockk<GameHubViewModel>(relaxed = true)
        val json = """{"exitData": {"social_network": "none"}}"""

        handleGameExit(json, null, gameHubViewModel)

        verify(exactly = 0) { AnalyticsManager.logCustomAdHocEvent(any(), any()) }
    }

    @Test
    fun `given events on an exit that does not dispatch OnGameExited, when handleGameExit, then events are still forwarded`() {
        val gameHubViewModel = mockk<GameHubViewModel>(relaxed = true)
        val json = """
            {
                "exitData": {"social_network": "none"},
                "events": [{"name": "did_play_game", "timestampMs": 1, "params": {}}]
            }
        """.trimIndent()

        handleGameExit(json, null, gameHubViewModel)

        verify(exactly = 1) { AnalyticsManager.logCustomAdHocEvent("did_play_game", any()) }
        verify(exactly = 0) { gameHubViewModel.onEvent(any()) }
    }
}
