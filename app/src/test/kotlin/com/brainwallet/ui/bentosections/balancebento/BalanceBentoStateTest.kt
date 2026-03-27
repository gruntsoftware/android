package com.brainwallet.ui.bentosections.balancebento

import com.brainwallet.data.model.CurrencyEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class BalanceBentoStateTest {

    // ── default state ──────────────────────────────────────────────────────

    @Test
    fun `default state has expected field values`() {
        val state = BalanceBentoState()

        assertTrue(state.darkMode)
        assertEquals("USD", state.fiatCode)
        assertEquals("$", state.symbol)
        assertEquals("Syncing...", state.topMessage)
        assertEquals("", state.lastTimeStamp)
        assertEquals(0.5f, state.syncProgress, 0.001f)
        assertEquals(0, state.currentBlockHeight)
        assertEquals(0L, state.ltcBalance)
        assertEquals(BigDecimal(0), state.litoshiBalance)
        assertTrue(state.balanceHidden)
        assertTrue(state.brainwalletIsSyncing)
        assertTrue(state.transactions.isEmpty())
        assertTrue(state.isInternetReachable)
    }

    @Test
    fun `default selectedCurrency is USD`() {
        val currency = BalanceBentoState().selectedCurrency
        assertEquals("USD", currency.code)
        assertEquals("$", currency.symbol)
    }

    // ── fiatBalance derived property ───────────────────────────────────────

    @Test
    fun `fiatBalance is litoshiBalance multiplied by currency rate`() {
        val state = BalanceBentoState(
            litoshiBalance = BigDecimal("1.0"),
            selectedCurrency = CurrencyEntity("USD", "US Dollar", 80.0f, "$")
        )
        assertEquals(80.0f, state.fiatBalance, 0.001f)
    }

    @Test
    fun `fiatBalance is zero when litoshiBalance is zero`() {
        val state = BalanceBentoState(
            litoshiBalance = BigDecimal.ZERO,
            selectedCurrency = CurrencyEntity("USD", "US Dollar", 80.0f, "$")
        )
        assertEquals(0.0f, state.fiatBalance, 0.001f)
    }

    @Test
    fun `fiatBalance is zero when currency rate is zero`() {
        val state = BalanceBentoState(
            litoshiBalance = BigDecimal("1.5"),
            selectedCurrency = CurrencyEntity("USD", "US Dollar", 0.0f, "$")
        )
        assertEquals(0.0f, state.fiatBalance, 0.001f)
    }

    @Test
    fun `fiatBalance scales correctly with fractional litoshiBalance`() {
        // 0.5 LTC at rate 100 = 50.0
        val state = BalanceBentoState(
            litoshiBalance = BigDecimal("0.5"),
            selectedCurrency = CurrencyEntity("GBP", "British Pound", 100.0f, "£")
        )
        assertEquals(50.0f, state.fiatBalance, 0.001f)
    }

    @Test
    fun `fiatBalance works with non-USD currency`() {
        val state = BalanceBentoState(
            litoshiBalance = BigDecimal("2.0"),
            selectedCurrency = CurrencyEntity("EUR", "Euro", 75.0f, "€")
        )
        assertEquals(150.0f, state.fiatBalance, 0.001f)
    }

    @Test
    fun `fiatBalance handles negative rate without crashing`() {
        // rate of -1f is the sentinel default in CurrencyEntity
        val state = BalanceBentoState(
            litoshiBalance = BigDecimal("1.0"),
            selectedCurrency = CurrencyEntity("USD", "US Dollar", -1f, "$")
        )
        assertEquals(-1.0f, state.fiatBalance, 0.001f)
    }

    // ── fiatBalanceFormatted derived property ──────────────────────────────

    @Test
    fun `fiatBalanceFormatted returns two decimal places`() {
        val state = BalanceBentoState(
            litoshiBalance = BigDecimal("1.0"),
            selectedCurrency = CurrencyEntity("USD", "US Dollar", 80.0f, "$")
        )
        assertEquals("80.00", state.fiatBalanceFormatted)
    }

    @Test
    fun `fiatBalanceFormatted rounds to two decimal places`() {
        val state = BalanceBentoState(
            litoshiBalance = BigDecimal("1.0"),
            selectedCurrency = CurrencyEntity("USD", "US Dollar", 80.555f, "$")
        )
        // float multiplication may not be exact — assert format shape
        assertTrue(state.fiatBalanceFormatted.matches(Regex("""\d+\.\d{2}""")))
    }

    @Test
    fun `fiatBalanceFormatted is 0_00 when balance is zero`() {
        val state = BalanceBentoState(
            litoshiBalance = BigDecimal.ZERO,
            selectedCurrency = CurrencyEntity("USD", "US Dollar", 80.0f, "$")
        )
        assertEquals("0.00", state.fiatBalanceFormatted)
    }

    @Test
    fun `fiatBalanceFormatted handles large balance without crashing`() {
        val state = BalanceBentoState(
            litoshiBalance = BigDecimal("21000000.0"),
            selectedCurrency = CurrencyEntity("USD", "US Dollar", 100.0f, "$")
        )
        assertEquals("2100000000.00", state.fiatBalanceFormatted)
    }

    // ── copy / immutability ────────────────────────────────────────────────

    @Test
    fun `copy produces independent state with changed field`() {
        val original = BalanceBentoState(ltcBalance = 1_000_000L)
        val copy = original.copy(ltcBalance = 2_000_000L)

        assertEquals(1_000_000L, original.ltcBalance)
        assertEquals(2_000_000L, copy.ltcBalance)
    }

    @Test
    fun `copy preserves unchanged fields`() {
        val original = BalanceBentoState(fiatCode = "GBP", syncProgress = 0.75f)
        val copy = original.copy(ltcBalance = 500L)

        assertEquals("GBP", copy.fiatCode)
        assertEquals(0.75f, copy.syncProgress, 0.001f)
    }

    @Test
    fun `two states with identical fields are equal`() {
        val a = BalanceBentoState(ltcBalance = 1_000L, fiatCode = "EUR")
        val b = BalanceBentoState(ltcBalance = 1_000L, fiatCode = "EUR")
        assertEquals(a, b)
    }

    // ── transactions ──────────────────────────────────────────────────────

    @Test
    fun `transactions list is empty by default`() {
        assertTrue(BalanceBentoState().transactions.isEmpty())
    }

    // ── sync state ────────────────────────────────────────────────────────

    @Test
    fun `brainwalletIsSyncing is true by default`() {
        assertTrue(BalanceBentoState().brainwalletIsSyncing)
    }

    @Test
    fun `state can represent fully synced condition`() {
        val state = BalanceBentoState(
            syncProgress = 1.0f,
            brainwalletIsSyncing = false
        )
        assertFalse(state.brainwalletIsSyncing)
        assertEquals(1.0f, state.syncProgress, 0.001f)
    }
}
