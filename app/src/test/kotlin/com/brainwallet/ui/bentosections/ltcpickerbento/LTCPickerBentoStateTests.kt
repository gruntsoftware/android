package com.brainwallet.ui.bentosections.ltcpickerbento
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.GlobalCurrency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test
class LTCPickerBentoStateTests {

    // ── default values ─────────────────────────────────────────────────────

    @Test
    fun `default state has correct values`() {
        val state = LTCPickerBentoState()
        assertTrue(state.darkMode)
        assertEquals("USD", state.selectedCurrency.code)
        assertEquals("", state.formattedTimeStamp)
        assertEquals(GlobalCurrency.entries, state.globalCurrencies)
        assertEquals(GlobalCurrency.USD, state.selectedGlobalCurrency)
    }

    // ── copy ───────────────────────────────────────────────────────────────

    @Test
    fun `copy produces new instance with only changed field updated`() {
        val original = LTCPickerBentoState()
        val updated = original.copy(darkMode = false)

        assertFalse(updated.darkMode)
        assertEquals(original.selectedCurrency, updated.selectedCurrency)
        assertEquals(original.formattedTimeStamp, updated.formattedTimeStamp)
        assertEquals(original.globalCurrencies, updated.globalCurrencies)
        assertEquals(original.selectedGlobalCurrency, updated.selectedGlobalCurrency)
    }

    @Test
    fun `copy with new selectedCurrency does not mutate original`() {
        val original = LTCPickerBentoState()
        val updated = original.copy(selectedCurrency = CurrencyEntity("EUR", "Euro", 1.1f, "€"))

        assertEquals("EUR", updated.selectedCurrency.code)
        assertEquals("USD", original.selectedCurrency.code)
    }

    @Test
    fun `copy with new formattedTimeStamp reflects new value`() {
        val state = LTCPickerBentoState().copy(formattedTimeStamp = "January 01, 2025 12:00:00 PM")
        assertEquals("January 01, 2025 12:00:00 PM", state.formattedTimeStamp)
    }

    // ── equals / hashCode ──────────────────────────────────────────────────

    @Test
    fun `two states with identical fields are equal`() {
        val a = LTCPickerBentoState()
        val b = LTCPickerBentoState()
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `two states with different darkMode are not equal`() {
        assertNotEquals(LTCPickerBentoState(darkMode = true), LTCPickerBentoState(darkMode = false))
    }

    @Test
    fun `two states with different selectedCurrency are not equal`() {
        val a = LTCPickerBentoState(selectedCurrency = CurrencyEntity("USD", "US Dollar", -1f, "$"))
        val b = LTCPickerBentoState(selectedCurrency = CurrencyEntity("EUR", "Euro", 1.1f, "€"))
        assertNotEquals(a, b)
    }

    // ── getSelectedFiatRateIndex ───────────────────────────────────────────

    @Test
    fun `getSelectedFiatRateIndex returns correct index for matching currency`() {
        val state = LTCPickerBentoState(
            selectedCurrency = CurrencyEntity("EUR", "Euro", 1.0f, "€"),
            globalCurrencies = listOf(GlobalCurrency.USD, GlobalCurrency.EUR, GlobalCurrency.GBP)
        )
        assertEquals(1, state.getSelectedFiatRateIndex())
    }

    @Test
    fun `getSelectedFiatRateIndex returns -1 when currency not found`() {
        val state = LTCPickerBentoState(
            selectedCurrency = CurrencyEntity("JPY", "Yen", 1.0f, "¥"),
            globalCurrencies = listOf(GlobalCurrency.USD, GlobalCurrency.EUR)
        )
        assertEquals(-1, state.getSelectedFiatRateIndex())
    }

    @Test
    fun `getSelectedFiatRateIndex returns -1 when globalCurrencies is empty`() {
        val state = LTCPickerBentoState(
            selectedCurrency = CurrencyEntity("USD", "US Dollar", -1f, "$"),
            globalCurrencies = emptyList()
        )
        assertEquals(-1, state.getSelectedFiatRateIndex())
    }

    @Test
    fun `getSelectedFiatRateIndex is case insensitive`() {
        val upper = LTCPickerBentoState(selectedCurrency = CurrencyEntity("EUR", "Euro", 1.0f, "€"))
        val lower = LTCPickerBentoState(selectedCurrency = CurrencyEntity("eur", "Euro", 1.0f, "€"))
        val mixed = LTCPickerBentoState(selectedCurrency = CurrencyEntity("eUr", "Euro", 1.0f, "€"))

        assertEquals(upper.getSelectedFiatRateIndex(), lower.getSelectedFiatRateIndex())
        assertEquals(upper.getSelectedFiatRateIndex(), mixed.getSelectedFiatRateIndex())
    }

    @Test
    fun `getSelectedFiatRateIndex returns index of first match when list has duplicates`() {
        val state = LTCPickerBentoState(
            selectedCurrency = CurrencyEntity("USD", "US Dollar", -1f, "$"),
            globalCurrencies = listOf(GlobalCurrency.USD, GlobalCurrency.USD, GlobalCurrency.EUR)
        )
        assertEquals(0, state.getSelectedFiatRateIndex())
    }
}
