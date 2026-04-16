package com.brainwallet.ui.bentosections.buyreceive

import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.MoonpayCurrencyLimit
import com.brainwallet.data.model.isCustom
import com.brainwallet.ui.bentosections.buyreceivebento.receive.ReceiveDialogState
import com.brainwallet.ui.bentosections.buyreceivebento.receive.getDefaultFiatAmount
import com.brainwallet.ui.bentosections.buyreceivebento.receive.getLtcAmountFormatted
import com.brainwallet.ui.bentosections.buyreceivebento.receive.getQuickFiatAmountOptions
import com.brainwallet.ui.bentosections.buyreceivebento.receive.getRatesUpdatedAtFormatted
import com.brainwallet.ui.bentosections.buyreceivebento.receive.getSelectedFiatCurrencyIndex
import com.brainwallet.ui.bentosections.buyreceivebento.receive.isQuickFiatAmountOptionCustom
import com.brainwallet.ui.bentosections.buyreceivebento.receive.moonpayWidgetVisible
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReceiveDialogStateTest {

    private val fakeCurrency = CurrencyEntity(
        code = "USD",
        name = "US Dollar",
        rate = 1.0f,
        symbol = "$"
    )

    // ── getSelectedFiatCurrencyIndex ───────────────────────────────────────

    @Test
    fun `getSelectedFiatCurrencyIndex returns correct index when currency exists`() {
        val gbp = CurrencyEntity("GBP", "British Pound", 0.79f, "£")
        val state = ReceiveDialogState(
            fiatCurrencies = listOf(fakeCurrency, gbp),
            selectedFiatCurrency = gbp
        )
        assertEquals(1, state.getSelectedFiatCurrencyIndex())
    }

    @Test
    fun `getSelectedFiatCurrencyIndex returns -1 when currency not in list`() {
        val state = ReceiveDialogState(
            fiatCurrencies = listOf(fakeCurrency),
            selectedFiatCurrency = CurrencyEntity("EUR", "Euro", 0.9f, "€")
        )
        assertEquals(-1, state.getSelectedFiatCurrencyIndex())
    }

    @Test
    fun `getSelectedFiatCurrencyIndex is case insensitive`() {
        val lower = CurrencyEntity("usd", "US Dollar", 1f, "$")
        val state = ReceiveDialogState(
            fiatCurrencies = listOf(fakeCurrency),
            selectedFiatCurrency = lower
        )
        assertEquals(0, state.getSelectedFiatCurrencyIndex())
    }

    // ── getDefaultFiatAmount ───────────────────────────────────────────────

    @Test
    fun `getDefaultFiatAmount returns min times 10`() {
        val limit = MoonpayCurrencyLimit()
        limit.data.baseCurrency.min = 5f
        val state = ReceiveDialogState(moonpayCurrencyLimit = limit)
        assertEquals(50f, state.getDefaultFiatAmount())
    }

    // ── getRatesUpdatedAtFormatted ─────────────────────────────────────────

    @Test
    fun `getRatesUpdatedAtFormatted returns non-empty uppercase string`() {
        val state = ReceiveDialogState(ratesUpdatedAt = 0L)
        val result = state.getRatesUpdatedAtFormatted()
        assertTrue(result.isNotEmpty())
        assertEquals(result, result.uppercase())
    }

    // ──────── This is looking for epoch ──────────────────────────────────────
    @Test
    fun `getRatesUpdatedAtFormatted formats epoch correctly`() {
        val state = ReceiveDialogState(ratesUpdatedAt = 0L)
        val result = state.getRatesUpdatedAtFormatted()
        assertTrue(result.matches(Regex("""^\d{1,2} [A-Z]{3} \d{4} \d{2}:\d{2}:\d{2}$""")))
    }

    // ── getLtcAmountFormatted ──────────────────────────────────────────────
    @Test
    fun `getLtcAmountFormatted returns placeholder when loading`() {
        val state = ReceiveDialogState(ltcAmount = 1.5f)
        assertEquals("x.xxxŁ", state.getLtcAmountFormatted(isLoading = true))
    }

    @Test
    fun `getLtcAmountFormatted returns placeholder when ltcAmount is negative`() {
        val state = ReceiveDialogState(ltcAmount = -1f)
        assertEquals("x.xxxŁ", state.getLtcAmountFormatted(isLoading = false))
    }

    @Test
    fun `getLtcAmountFormatted returns formatted value when not loading`() {
        val state = ReceiveDialogState(ltcAmount = 2.5f)
        assertEquals("2.500Ł", state.getLtcAmountFormatted(isLoading = false))
    }

    @Test
    fun `getLtcAmountFormatted formats to 3 decimal places`() {
        val state = ReceiveDialogState(ltcAmount = 1.23456f)
        val result = state.getLtcAmountFormatted(isLoading = false)
        assertTrue(result.endsWith("Ł"))
        assertTrue(result.substringBefore("Ł").substringAfter(".").length == 3)
    }

    // ── getQuickFiatAmountOptions ──────────────────────────────────────────

    @Test
    fun `getQuickFiatAmountOptions returns 4 options`() {
        val state = ReceiveDialogState(selectedFiatCurrency = fakeCurrency)
        assertEquals(4, state.getQuickFiatAmountOptions().size)
    }

    @Test
    fun `getQuickFiatAmountOptions last option is custom`() {
        val state = ReceiveDialogState(selectedFiatCurrency = fakeCurrency)
        val options = state.getQuickFiatAmountOptions()
        assertTrue(options.last().isCustom())
    }

    @Test
    fun `getQuickFiatAmountOptions uses selected fiat currency symbol`() {
        val gbp = CurrencyEntity("GBP", "British Pound", 0.79f, "£")
        val state = ReceiveDialogState(selectedFiatCurrency = gbp)
        val nonCustomOptions = state.getQuickFiatAmountOptions().dropLast(1)
        assertTrue(nonCustomOptions.all { it.symbol == "£" })
    }

    // ── isQuickFiatAmountOptionCustom ──────────────────────────────────────

    @Test
    fun `isQuickFiatAmountOptionCustom returns true when index is 3`() {
        val state = ReceiveDialogState(selectedQuickFiatAmountOptionIndex = 3)
        assertTrue(state.isQuickFiatAmountOptionCustom())
    }

    @Test
    fun `isQuickFiatAmountOptionCustom returns false when index is not 3`() {
        val state = ReceiveDialogState(selectedQuickFiatAmountOptionIndex = 1)
        assertFalse(state.isQuickFiatAmountOptionCustom())
    }

    // ── moonpayWidgetVisible ───────────────────────────────────────────────

    @Test
    fun `moonpayWidgetVisible returns true when signedUrl is not null`() {
        val state = ReceiveDialogState(moonpayBuySignedUrl = "https://moonpay.com/signed")
        assertTrue(state.moonpayWidgetVisible())
    }

    @Test
    fun `moonpayWidgetVisible returns false when signedUrl is null`() {
        val state = ReceiveDialogState(moonpayBuySignedUrl = null)
        assertFalse(state.moonpayWidgetVisible())
    }
}
