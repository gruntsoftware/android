package com.brainwallet.gameinterface

import com.brainwallet.ui.screens.gamehub.GameHubEvent
import com.brainwallet.ui.screens.gamehub.GameHubViewModel
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.serialization.json.Json
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class GdxGameViewTest {

    private val lenientJson = Json { ignoreUnknownKeys = true }

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

    // ── handleGameExit dispatch logic ─────────────────────────────────────

    @Test
    fun `given twitter exit with screenshot, when handleGameExit, then dispatches OnGameExited with the screenshot`() {
        val gameHubViewModel = mockk<GameHubViewModel>(relaxed = true)
        val screenshot = byteArrayOf(1, 2, 3, 4)
        val json = """{"social_network":"twitter","total_score":100}"""

        handleGameExit(json, screenshot, gameHubViewModel)

        val slot = slot<GameHubEvent>()
        verify(exactly = 1) { gameHubViewModel.onEvent(capture(slot)) }

        val captured = slot.captured as GameHubEvent.OnGameExited
        assertEquals(json, captured.jsonPayload)
        assertArrayEquals(screenshot, captured.byteArray)
    }

    @Test
    fun `given instagram exit with screenshot, when handleGameExit, then dispatches OnGameExited with the screenshot`() {
        val gameHubViewModel = mockk<GameHubViewModel>(relaxed = true)
        val screenshot = byteArrayOf(5, 6, 7)
        val json = """{"social_network":"instagram"}"""

        handleGameExit(json, screenshot, gameHubViewModel)

        val slot = slot<GameHubEvent>()
        verify(exactly = 1) { gameHubViewModel.onEvent(capture(slot)) }

        val captured = slot.captured as GameHubEvent.OnGameExited
        assertArrayEquals(screenshot, captured.byteArray)
    }

    @Test
    fun `given twitter exit with null screenshot, when handleGameExit, then does not dispatch an event`() {
        val gameHubViewModel = mockk<GameHubViewModel>(relaxed = true)
        val json = """{"social_network":"twitter"}"""

        handleGameExit(json, null, gameHubViewModel)

        verify(exactly = 0) { gameHubViewModel.onEvent(any()) }
    }

    @Test
    fun `given twitter exit with empty screenshot, when handleGameExit, then does not dispatch an event`() {
        val gameHubViewModel = mockk<GameHubViewModel>(relaxed = true)
        val json = """{"social_network":"twitter"}"""

        handleGameExit(json, ByteArray(0), gameHubViewModel)

        verify(exactly = 0) { gameHubViewModel.onEvent(any()) }
    }

    @Test
    fun `given exit with no social network, when handleGameExit, then does not dispatch an event`() {
        val gameHubViewModel = mockk<GameHubViewModel>(relaxed = true)
        val json = """{"total_score":50}"""

        handleGameExit(json, byteArrayOf(1), gameHubViewModel)

        verify(exactly = 0) { gameHubViewModel.onEvent(any()) }
    }

    @Test
    fun `given exit with unrecognised social network, when handleGameExit, then does not dispatch an event`() {
        val gameHubViewModel = mockk<GameHubViewModel>(relaxed = true)
        val json = """{"social_network":"facebook"}"""

        handleGameExit(json, byteArrayOf(1), gameHubViewModel)

        verify(exactly = 0) { gameHubViewModel.onEvent(any()) }
    }

    @Test
    fun `given malformed json, when handleGameExit, then does not throw and does not dispatch an event`() {
        val gameHubViewModel = mockk<GameHubViewModel>(relaxed = true)

        handleGameExit("not valid json", byteArrayOf(1), gameHubViewModel)

        verify(exactly = 0) { gameHubViewModel.onEvent(any()) }
    }
}
