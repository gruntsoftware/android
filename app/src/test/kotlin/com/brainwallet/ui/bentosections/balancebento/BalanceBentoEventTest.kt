package com.brainwallet.ui.bentosections.balancebento

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BalanceBentoEventTest {

    // ── type identity ──────────────────────────────────────────────────────

    @Test
    fun `OnLoad is instance of BalanceBentoEvent`() {
        assertTrue(BalanceBentoEvent.OnLoad is BalanceBentoEvent)
    }

    @Test
    fun `OnToggleBalanceVisibility is instance of BalanceBentoEvent`() {
        assertTrue(BalanceBentoEvent.OnToggleBalanceVisibility is BalanceBentoEvent)
    }

    // ── singleton equality ─────────────────────────────────────────────────

    @Test
    fun `OnLoad equals itself`() {
        assertEquals(BalanceBentoEvent.OnLoad, BalanceBentoEvent.OnLoad)
    }

    @Test
    fun `OnToggleBalanceVisibility equals itself`() {
        assertEquals(BalanceBentoEvent.OnToggleBalanceVisibility, BalanceBentoEvent.OnToggleBalanceVisibility)
    }

    @Test
    fun `OnLoad and OnToggleBalanceVisibility are not equal`() {
        assertNotEquals(BalanceBentoEvent.OnLoad, BalanceBentoEvent.OnToggleBalanceVisibility)
    }

    // ── when exhaustiveness ───────────────────────────────────────────────

    @Test
    fun `when expression covers all sealed subclasses`() {
        val events: List<BalanceBentoEvent> = listOf(
            BalanceBentoEvent.OnLoad,
            BalanceBentoEvent.OnToggleBalanceVisibility,
        )
        val handled = events.map { event ->
            when (event) {
                is BalanceBentoEvent.OnLoad -> "load"
                is BalanceBentoEvent.OnToggleBalanceVisibility -> "toggle"
            }
        }
        assertEquals(listOf("load", "toggle"), handled)
    }
}
