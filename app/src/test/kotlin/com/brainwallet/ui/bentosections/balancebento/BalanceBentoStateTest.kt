package com.brainwallet.ui.bentosections.balancebento
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class BalanceBentoStateTest {

    @Test
    fun `default state has expected values`() {
        val state = BalanceBentoState()

        assertTrue(state.darkMode)
        assertEquals("USD", state.fiatCode)
        assertEquals("$", state.symbol)
        assertEquals("Syncing...", state.topMessage)
        assertEquals("", state.lastTimeStamp)
        assertEquals(0.5f, state.syncProgress)
        assertEquals(0, state.currentBlockHeight)
        assertTrue(state.balanceHidden)
        assertTrue(state.brainwalletIsSyncing)
        assertTrue(state.transactions.isEmpty())
        assertTrue(state.isInternetReachable)
        assertNull(state.ltcStats)
    }

    @Test
    fun `copy preserves unchanged fields`() {
        val state = BalanceBentoState().copy(fiatCode = "GBP")

        assertEquals("GBP", state.fiatCode)
        assertEquals("$", state.symbol) // unchanged
        assertTrue(state.darkMode) // unchanged
    }

    @Test
    fun `darkMode can be toggled`() {
        val state = BalanceBentoState(darkMode = false)
        assertFalse(state.darkMode)

        val toggled = state.copy(darkMode = !state.darkMode)
        assertTrue(toggled.darkMode)
    }

    @Test
    fun `balanceHidden can be toggled`() {
        val state = BalanceBentoState(balanceHidden = true)
        val revealed = state.copy(balanceHidden = false)
        assertFalse(revealed.balanceHidden)
    }

    @Test
    fun `syncProgress bounds are valid`() {
        val syncing = BalanceBentoState(syncProgress = 0.0f)
        assertEquals(0.0f, syncing.syncProgress)

        val complete = BalanceBentoState(syncProgress = 1.0f)
        assertEquals(1.0f, complete.syncProgress)
    }

    @Test
    fun `brainwalletIsSyncing false reflects sync complete`() {
        val state = BalanceBentoState(
            brainwalletIsSyncing = false,
            syncProgress = 1.0f,
            topMessage = "Synced"
        )
        assertFalse(state.brainwalletIsSyncing)
        assertEquals(1.0f, state.syncProgress)
        assertEquals("Synced", state.topMessage)
    }

    @Test
    fun `isInternetReachable false reflects offline state`() {
        val state = BalanceBentoState(isInternetReachable = false)
        assertFalse(state.isInternetReachable)
    }

    @Test
    fun `two default states are equal`() {
        val a = BalanceBentoState()
        val b = BalanceBentoState()
        assertEquals(a, b)
    }

    @Test
    fun `states with different fiatCode are not equal`() {
        val usd = BalanceBentoState(fiatCode = "USD")
        val eur = BalanceBentoState(fiatCode = "EUR")
        assertNotEquals(usd, eur)
    }

    @Test
    fun `currentBlockHeight updates correctly`() {
        val state = BalanceBentoState(currentBlockHeight = 0)
        val updated = state.copy(currentBlockHeight = 2_530_000)
        assertEquals(2_530_000, updated.currentBlockHeight)
    }

    @Test
    fun `lastTimeStamp can be set`() {
        val state = BalanceBentoState(lastTimeStamp = "2024-01-15 10:30")
        assertEquals("2024-01-15 10:30", state.lastTimeStamp)
    }
}
