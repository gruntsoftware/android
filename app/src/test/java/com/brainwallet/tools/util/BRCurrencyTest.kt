package com.brainwallet.tools.util

import com.brainwallet.constants.BWConstants
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Locale

class BRCurrencyTest {

    private lateinit var originalLocale: Locale

    @Before
    fun setUp() {
        originalLocale = Locale.getDefault()
        Locale.setDefault(Locale.US)
    }

    @After
    fun tearDown() {
        Locale.setDefault(originalLocale)
    }

    @Test
    fun `given LTC iso, when getMaxDecimalPlaces, then returns 8 regardless of case`() {
        assertEquals(8, BRCurrency.getMaxDecimalPlaces("LTC"))
        assertEquals(8, BRCurrency.getMaxDecimalPlaces("ltc"))
    }

    @Test
    fun `given null or empty iso, when getMaxDecimalPlaces, then defaults to 8`() {
        assertEquals(8, BRCurrency.getMaxDecimalPlaces(null))
        assertEquals(8, BRCurrency.getMaxDecimalPlaces(""))
    }

    @Test
    fun `given fiat iso with two decimal digits, when getMaxDecimalPlaces, then returns that currency's fraction digits`() {
        assertEquals(2, BRCurrency.getMaxDecimalPlaces("USD"))
        assertEquals(2, BRCurrency.getMaxDecimalPlaces("EUR"))
    }

    @Test
    fun `given fiat iso with zero decimal digits, when getMaxDecimalPlaces, then returns zero`() {
        assertEquals(0, BRCurrency.getMaxDecimalPlaces("JPY"))
    }

    @Test
    fun `given LTC iso and null context, when getSymbolByIso, then returns lowercase litecoin symbol`() {
        assertEquals(BWConstants.litecoinLowercase, BRCurrency.getSymbolByIso(null, "LTC"))
    }

    @Test
    fun `given known fiat iso, when getSymbolByIso, then returns that currency's symbol`() {
        assertEquals("$", BRCurrency.getSymbolByIso(null, "USD"))
        assertEquals("€", BRCurrency.getSymbolByIso(null, "EUR"))
    }

    @Test
    fun `given an unrecognized iso, when getSymbolByIso, then falls back to the default locale's currency symbol`() {
        // "XYZ" is not a valid ISO 4217 code, so this exercises the IllegalArgumentException
        // fallback path, which resolves the default locale's currency instead (US -> "$").
        assertEquals("$", BRCurrency.getSymbolByIso(null, "XYZ"))
    }

    @Test
    fun `given null context, when getCurrencyName, then returns the iso unchanged`() {
        assertEquals("LTC", BRCurrency.getCurrencyName(null, "LTC"))
        assertEquals("USD", BRCurrency.getCurrencyName(null, "USD"))
    }
}
