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

    @Test
    fun `OnUpdatedSyncProgress is instance of BalanceBentoEvent`() {
        assertTrue(BalanceBentoEvent.OnUpdatedSyncProgress(0.5f) is BalanceBentoEvent)
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

    // ── OnUpdatedSyncProgress data class ───────────────────────────────────

    @Test
    fun `OnUpdatedSyncProgress holds syncProgress value`() {
        val event = BalanceBentoEvent.OnUpdatedSyncProgress(0.75f)
        assertEquals(0.75f, event.syncProgress, 0.001f)
    }

    @Test
    fun `OnUpdatedSyncProgress with same value are equal`() {
        val a = BalanceBentoEvent.OnUpdatedSyncProgress(0.5f)
        val b = BalanceBentoEvent.OnUpdatedSyncProgress(0.5f)
        assertEquals(a, b)
    }

    @Test
    fun `OnUpdatedSyncProgress with different values are not equal`() {
        val a = BalanceBentoEvent.OnUpdatedSyncProgress(0.5f)
        val b = BalanceBentoEvent.OnUpdatedSyncProgress(0.9f)
        assertNotEquals(a, b)
    }

    @Test
    fun `OnUpdatedSyncProgress copy produces new instance with updated value`() {
        val original = BalanceBentoEvent.OnUpdatedSyncProgress(0.3f)
        val copy = original.copy(syncProgress = 0.8f)
        assertEquals(0.8f, copy.syncProgress, 0.001f)
        assertEquals(0.3f, original.syncProgress, 0.001f)
    }

    @Test
    fun `OnUpdatedSyncProgress accepts zero`() {
        val event = BalanceBentoEvent.OnUpdatedSyncProgress(0.0f)
        assertEquals(0.0f, event.syncProgress, 0.001f)
    }

    @Test
    fun `OnUpdatedSyncProgress accepts one`() {
        val event = BalanceBentoEvent.OnUpdatedSyncProgress(1.0f)
        assertEquals(1.0f, event.syncProgress, 0.001f)
    }

    // ── when exhaustiveness ───────────────────────────────────────────────

    @Test
    fun `when expression covers all sealed subclasses`() {
        val events: List<BalanceBentoEvent> = listOf(
            BalanceBentoEvent.OnLoad,
            BalanceBentoEvent.OnToggleBalanceVisibility,
            BalanceBentoEvent.OnUpdatedSyncProgress(0.5f)
        )
        val handled = events.map { event ->
            when (event) {
                is BalanceBentoEvent.OnLoad -> "load"
                is BalanceBentoEvent.OnToggleBalanceVisibility -> "toggle"
                is BalanceBentoEvent.OnUpdatedSyncProgress -> "progress"
            }
        }
        assertEquals(listOf("load", "toggle", "progress"), handled)
    }
}
