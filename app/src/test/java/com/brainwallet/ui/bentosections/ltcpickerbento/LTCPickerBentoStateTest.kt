package com.brainwallet.ui.bentosections.ltcpickerbento

import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.GlobalCurrency
import org.junit.Assert.assertEquals
import org.junit.Test

class LTCPickerBentoStateTest {

    @Test
    fun `initial state has correct default values`() {
        val state = LTCPickerBentoState()

        assertEquals("USD", state.selectedCurrency.code)
        assertEquals(GlobalCurrency.USD, state.selectedGlobalCurrency)
        assertEquals(true, state.darkMode)
    }

    @Test
    fun `getSelectedFiatRateIndex returns correct index when codes match`() {
        // Arrange: Create a state where EUR is the selected currency
        val state = LTCPickerBentoState(
            selectedCurrency = CurrencyEntity("EUR", "Euro", 1.0f, "€"),
            globalCurrencies = listOf(GlobalCurrency.USD, GlobalCurrency.EUR, GlobalCurrency.GBP)
        )

        // Act
        val actualIndex = state.getSelectedFiatRateIndex()

        // Assert: EUR is at index 1
        assertEquals(1, actualIndex)
    }

    @Test
    fun `getSelectedFiatRateIndex is case insensitive`() {
        // Arrange: Entity has lowercase 'eur', GlobalCurrency enum has uppercase 'EUR'
        val state = LTCPickerBentoState(
            selectedCurrency = CurrencyEntity("eur", "Euro", 1.0f, "€"),
            globalCurrencies = listOf(GlobalCurrency.USD, GlobalCurrency.EUR)
        )

        // Act
        val actualIndex = state.getSelectedFiatRateIndex()

        // Assert
        assertEquals(1, actualIndex)
    }

    @Test
    fun `getSelectedFiatRateIndex returns negative one when currency not found`() {
        // Arrange: Searching for a currency not in the list
        val state = LTCPickerBentoState(
            selectedCurrency = CurrencyEntity("JPY", "Yen", 1.0f, "¥"),
            globalCurrencies = listOf(GlobalCurrency.USD, GlobalCurrency.EUR)
        )

        // Act
        val actualIndex = state.getSelectedFiatRateIndex()

        // Assert
        assertEquals(-1, actualIndex)
    }
}
