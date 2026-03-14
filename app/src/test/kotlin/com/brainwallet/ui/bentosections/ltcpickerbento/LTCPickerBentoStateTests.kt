package com.brainwallet.ui.bentosections.ltcpickerbento
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.GlobalCurrency
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class LTCPickerBentoStateTests {

    // ─────────────────────────────────────────────────────────────────────────
    // Default values
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `default state has darkMode true`() {
        assertTrue(LTCPickerBentoState().darkMode)
    }

    @Test
    fun `default state has USD as selectedCurrency`() {
        assertEquals("USD", LTCPickerBentoState().selectedCurrency.code)
    }

    @Test
    fun `default state has USD iso`() {
        assertEquals("USD", LTCPickerBentoState().iso)
    }

    @Test
    fun `default state has empty formattedTimeStamp`() {
        assertEquals("", LTCPickerBentoState().formattedTimeStamp)
    }

    @Test
    fun `default state has all GlobalCurrency entries`() {
        assertEquals(GlobalCurrency.entries, LTCPickerBentoState().globalCurrencies)
    }

    @Test
    fun `default state has USD as selectedGlobalCurrency`() {
        assertEquals(GlobalCurrency.USD, LTCPickerBentoState().selectedGlobalCurrency)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // copy() — data class contract
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `copy produces a new instance with only the changed field updated`() {
        val original = LTCPickerBentoState()
        val updated = original.copy(darkMode = false)

        assertFalse(updated.darkMode)
        // All other fields are unchanged
        assertEquals(original.selectedCurrency, updated.selectedCurrency)
        assertEquals(original.iso, updated.iso)
        assertEquals(original.formattedTimeStamp, updated.formattedTimeStamp)
        assertEquals(original.globalCurrencies, updated.globalCurrencies)
        assertEquals(original.selectedGlobalCurrency, updated.selectedGlobalCurrency)
    }

    @Test
    fun `copy with new selectedCurrency does not mutate original`() {
        val original = LTCPickerBentoState()
        val eurEntity = CurrencyEntity("EUR", "Euro", 1.1f, "€")

        val updated = original.copy(selectedCurrency = eurEntity)

        assertEquals("EUR", updated.selectedCurrency.code)
        assertEquals("USD", original.selectedCurrency.code) // original unchanged
    }

    @Test
    fun `copy with new formattedTimeStamp reflects the new value`() {
        val state = LTCPickerBentoState().copy(formattedTimeStamp = "January 01, 2025 12:00:00 PM")

        assertEquals("January 01, 2025 12:00:00 PM", state.formattedTimeStamp)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // equals / hashCode — data class contract
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `two states with identical fields are equal`() {
        val a = LTCPickerBentoState()
        val b = LTCPickerBentoState()

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `two states with different darkMode are not equal`() {
        val a = LTCPickerBentoState(darkMode = true)
        val b = LTCPickerBentoState(darkMode = false)

        assertNotEquals(a, b)
    }

    @Test
    fun `two states with different selectedCurrency are not equal`() {
        val a = LTCPickerBentoState(selectedCurrency = CurrencyEntity("USD", "US Dollar", -1f, "$"))
        val b = LTCPickerBentoState(selectedCurrency = CurrencyEntity("EUR", "Euro", 1.1f, "€"))

        assertNotEquals(a, b)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getSelectedFiatRateIndex
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    fun `getSelectedFiatRateIndex returns correct index for USD`() {
        val state = LTCPickerBentoState(
            selectedCurrency = CurrencyEntity("USD", "US Dollar", -1f, "$"),
            globalCurrencies = GlobalCurrency.entries
        )
        val expected = GlobalCurrency.entries.indexOfFirst {
            it.code.lowercase() == "usd"
        }

        assertEquals(expected, state.getSelectedFiatRateIndex())
        assertNotEquals(-1, state.getSelectedFiatRateIndex())
    }

    @Test
    fun `getSelectedFiatRateIndex returns correct index for EUR`() {
        val state = LTCPickerBentoState(
            selectedCurrency = CurrencyEntity("EUR", "Euro", 1.1f, "€"),
            globalCurrencies = GlobalCurrency.entries
        )
        val expected = GlobalCurrency.entries.indexOfFirst {
            it.code.lowercase() == "eur"
        }

        assertEquals(expected, state.getSelectedFiatRateIndex())
        assertNotEquals(-1, state.getSelectedFiatRateIndex())
    }

    @Test
    fun `getSelectedFiatRateIndex returns -1 when currency code has no match`() {
        val state = LTCPickerBentoState(
            selectedCurrency = CurrencyEntity("XYZ", "Unknown", -1f, "?"),
            globalCurrencies = GlobalCurrency.entries
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
    fun `getSelectedFiatRateIndex is case-insensitive for uppercase currency code`() {
        val upper = LTCPickerBentoState(
            selectedCurrency = CurrencyEntity("EUR", "Euro", 1.1f, "€")
        )
        val lower = LTCPickerBentoState(
            selectedCurrency = CurrencyEntity("eur", "Euro", 1.1f, "€")
        )

        assertEquals(upper.getSelectedFiatRateIndex(), lower.getSelectedFiatRateIndex())
    }

    @Test
    fun `getSelectedFiatRateIndex is case-insensitive for mixed-case currency code`() {
        val state = LTCPickerBentoState(
            selectedCurrency = CurrencyEntity("eUr", "Euro", 1.1f, "€")
        )
        val expected = GlobalCurrency.entries.indexOfFirst {
            it.code.lowercase() == "eur"
        }

        assertEquals(expected, state.getSelectedFiatRateIndex())
    }

    @Test
    fun `getSelectedFiatRateIndex returns index of first match when list has duplicates`() {
        val duplicate = GlobalCurrency.USD
        val state = LTCPickerBentoState(
            selectedCurrency = CurrencyEntity("USD", "US Dollar", -1f, "$"),
            globalCurrencies = listOf(duplicate, duplicate, GlobalCurrency.EUR)
        )

        assertEquals(0, state.getSelectedFiatRateIndex())
    }
}
