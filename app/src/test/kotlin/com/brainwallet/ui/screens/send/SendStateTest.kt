package com.brainwallet.ui.screens.send

import com.brainwallet.data.model.CurrencyEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class SendStateTest {
    @Test
    fun `default state has expected initial values`() {
        val state = SendState()

        assertFalse(state.biometricEnabled)
        assertFalse(state.darkMode)
        assertEquals("$ USD", state.formattedCurrency)
        assertEquals("", state.recipientLTCAddress)
        assertEquals("", state.userMemorandum)
        assertEquals(BigDecimal(0), state.networkFees)
        assertEquals(BigDecimal(0), state.serviceFees)
        assertEquals(BigDecimal(0), state.amountInLitoshi)
        assertEquals("", state.amountString)
        assertFalse(state.userViewsFiat)
        assertFalse(state.isReadyToSend)
        assertFalse(state.brainwalletIsPublishing)
        assertFalse(state.isLTCAddressValid)
        assertFalse(state.isAmountBelowBalance)
        assertTrue(state.passcode.isEmpty())
        assertFalse(state.isPasscodeAuthenticated)
    }

    @Test
    fun `default selectedCurrency is USD`() {
        val state = SendState()

        assertEquals("USD", state.selectedCurrency.code)
        assertEquals("US Dollar", state.selectedCurrency.name)
        assertEquals(-1f, state.selectedCurrency.rate)
        assertEquals("$", state.selectedCurrency.symbol)
    }

    @Test
    fun `copy updates amountString`() {
        val state = SendState().copy(amountString = "1.5")

        assertEquals("1.5", state.amountString)
    }

    @Test
    fun `copy updates recipientLTCAddress`() {
        val address = "LTC1abcdef123456"
        val state = SendState().copy(recipientLTCAddress = address)

        assertEquals(address, state.recipientLTCAddress)
    }

    @Test
    fun `copy updates isReadyToSend`() {
        val state = SendState().copy(isReadyToSend = true)

        assertTrue(state.isReadyToSend)
    }

    @Test
    fun `copy updates userViewsFiat`() {
        val state = SendState().copy(userViewsFiat = true)

        assertTrue(state.userViewsFiat)
    }

    @Test
    fun `copy updates passcode list`() {
        val state = SendState().copy(passcode = listOf(1, 2, 3, 4))

        assertEquals(listOf(1, 2, 3, 4), state.passcode)
        assertEquals(4, state.passcode.size)
    }

    @Test
    fun `copy appends digit to passcode`() {
        val state = SendState()
            .copy(passcode = emptyList<Int>() + 1 + 2 + 3)

        assertEquals(3, state.passcode.size)
        assertEquals(1, state.passcode[0])
        assertEquals(3, state.passcode[2])
    }

    @Test
    fun `copy dropsLast from passcode`() {
        val state = SendState()
            .copy(passcode = listOf(1, 2, 3, 4))
            .copy(passcode = listOf(1, 2, 3, 4).dropLast(1))

        assertEquals(listOf(1, 2, 3), state.passcode)
    }

    @Test
    fun `copy updates isPasscodeAuthenticated`() {
        val state = SendState().copy(isPasscodeAuthenticated = true)

        assertTrue(state.isPasscodeAuthenticated)
    }

    @Test
    fun `copy updates networkFees and serviceFees`() {
        val network = BigDecimal("1000")
        val service = BigDecimal("500")
        val state = SendState().copy(networkFees = network, serviceFees = service)

        assertEquals(network, state.networkFees)
        assertEquals(service, state.serviceFees)
    }

    @Test
    fun `copy updates selectedCurrency`() {
        val currency = CurrencyEntity("EUR", "Euro", 1.1f, "€")
        val state = SendState().copy(selectedCurrency = currency)

        assertEquals("EUR", state.selectedCurrency.code)
        assertEquals("€", state.selectedCurrency.symbol)
        assertEquals(1.1f, state.selectedCurrency.rate)
    }

    @Test
    fun `copy clears amountString on edit`() {
        val state = SendState()
            .copy(amountString = "5.0")
            .copy(amountString = "")

        assertEquals("", state.amountString)
    }

    @Test
    fun `two states with different values are not equal`() {
        val state1 = SendState(amountString = "1.0")
        val state2 = SendState(amountString = "2.0")

        assertTrue(state1 != state2)
    }
}
