package com.brainwallet.ui.screens.send

import android.app.Application
import com.brainwallet.appreview.InAppReviewService
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.data.repository.TxRepository
import com.brainwallet.presenter.entities.TransactionItem
import com.brainwallet.tools.manager.AnalyticsManager
import com.brainwallet.tools.security.BRKeyStore
import com.brainwallet.tools.util.Utils
import com.brainwallet.util.CurrencyDataGetter
import com.brainwallet.util.EventBus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

@OptIn(ExperimentalCoroutinesApi::class)
class SendViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var app: Application
    private lateinit var bwSender: BWSender
    private lateinit var txRepository: TxRepository
    private lateinit var settingRepository: SettingRepository
    private lateinit var currencyDataGetter: CurrencyDataGetter
    private lateinit var inAppReviewService: InAppReviewService

    private val usdCurrency = CurrencyEntity("USD", "US Dollar", 100f, "$")

    private val settingsFlow = MutableStateFlow(
        AppSetting(isDarkMode = false, currency = usdCurrency)
    )

    private fun TestScope.buildViewModel(
        getBalance: () -> Long = { 1_000_000_000L },
        validateAddress: (String) -> Boolean = { it.startsWith("L") },
        getCurrentFee: () -> Long = { 2_000L },
        getOpsFee: (Long) -> Long = { 500L },
        isWalletCreated: () -> Boolean = { true }
    ): SendViewModel = SendViewModel(
        app = app,
        bwSender = bwSender,
        txRepository = txRepository,
        settingRepository = settingRepository,
        isWalletCreated = isWalletCreated,
        validateAddress = validateAddress,
        getBalance = getBalance,
        getCurrentFee = getCurrentFee,
        getOpsFee = getOpsFee,
        currencyDataGetter = currencyDataGetter,
        inAppReviewService = inAppReviewService
    ).also { advanceUntilIdle() }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        app = mockk(relaxed = true) {
            every { resources.getString(any()) } returns "error"
            every { getString(any()) } returns "error"
        }
        bwSender = mockk(relaxed = true)
        txRepository = mockk(relaxed = true)
        settingRepository = mockk(relaxed = true) {
            every { currentSettings } returns settingsFlow
        }

        // Default: getCurrencyByIso returns USD at 100f
        currencyDataGetter = mockk {
            every { getCurrencyByIso("USD") } returns usdCurrency
        }
        inAppReviewService = mockk(relaxed = true)

        mockkStatic(BRKeyStore::class)
        every { BRKeyStore.getPinCode(any()) } returns "1234"

        mockkStatic(Utils::class)
        every { Utils.fetchServiceItem(any(), any()) } returns "LOpsAddr"
        every { Utils.tieredOpsFee(any(), any()) } returns 500L

        mockkStatic(AnalyticsManager::class)
        every { AnalyticsManager.logCustomEvent(any()) } returns Unit
        every { AnalyticsManager.logCustomEventWithParams(any(), any()) }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    // -------------------------------------------------------------------------
    // Initial state & settings propagation
    // -------------------------------------------------------------------------

    @Test
    fun `initial state is not ready to send`() = runTest {
        val vm = buildViewModel()
        val s = vm.state.value
        assertFalse("fresh VM must not have Send enabled", s.isReadyToSend)
        assertFalse(s.isLTCAddressValid)
        assertFalse(s.isAmountBelowBalance)
        assertFalse(s.brainwalletIsPublishing)
    }

    @Test
    fun `settings update propagates currency and darkMode`() = runTest {
        val vm = buildViewModel()
        val newCurrency = CurrencyEntity("GBP", "Pound", 80f, "£")
        every { currencyDataGetter.getCurrencyByIso("GBP") } returns newCurrency

        settingsFlow.value = AppSetting(isDarkMode = true, currency = newCurrency)
        advanceUntilIdle()

        assertTrue(vm.state.value.darkMode)
        assertEquals("GBP", vm.state.value.selectedCurrency.code)
    }

    // -------------------------------------------------------------------------
    // OnToggleFiatOrLTC — uses currencyDataGetter (not selectedCurrency.rate)
    // -------------------------------------------------------------------------

    @Test
    fun `toggle does not crash when currencyDataGetter returns no rate`() = runTest {
        // Simulates the case where rates haven't loaded yet — getCurrencyByIso
        // returns null, so the takeIf guard fires and convertedAmount stays null.
        every { currencyDataGetter.getCurrencyByIso(any()) } returns null

        val vm = buildViewModel()
        vm.onEvent(SendEvent.OnAmountChanged("0.5"))
        advanceUntilIdle()
        vm.onEvent(SendEvent.OnToggleFiatOrLTC)
        advanceUntilIdle()

        // amountString must be unchanged; VM must remain functional
        assertEquals("0.5", vm.state.value.amountString)
        assertFalse(vm.state.value.brainwalletIsPublishing)
    }

    @Test
    fun `toggle does not crash when rate is zero`() = runTest {
        // rate = 0f fails the takeIf { it > 0f } guard — same safe path as null
        every { currencyDataGetter.getCurrencyByIso("USD") } returns
            CurrencyEntity("USD", "US Dollar", 0f, "$")

        val vm = buildViewModel()
        vm.onEvent(SendEvent.OnAmountChanged("0.5"))
        advanceUntilIdle()
        vm.onEvent(SendEvent.OnToggleFiatOrLTC)
        advanceUntilIdle()

        assertEquals("0.5", vm.state.value.amountString)
    }

    @Test
    fun `toggle LTC to fiat multiplies by rate with 2dp rounding`() = runTest {
        val vm = buildViewModel()
        // Start in LTC view, enter 0.5 LTC
        vm.onEvent(SendEvent.OnAmountChanged("0.5"))
        advanceUntilIdle()

        vm.onEvent(SendEvent.OnToggleFiatOrLTC)
        advanceUntilIdle()

        assertTrue("should now view fiat", vm.state.value.userViewsFiat)
        // 0.5 * 100 = 50.00
        assertEquals(BigDecimal("50.00"), BigDecimal(vm.state.value.amountString))
    }

    @Test
    fun `toggle fiat to LTC divides by rate with 8dp precision`() = runTest {
        val vm = buildViewModel()
        vm.onEvent(SendEvent.OnToggleFiatOrLTC) // → fiat view
        advanceUntilIdle()
        vm.onEvent(SendEvent.OnAmountChanged("50"))
        advanceUntilIdle()

        vm.onEvent(SendEvent.OnToggleFiatOrLTC) // → back to LTC
        advanceUntilIdle()

        assertFalse(vm.state.value.userViewsFiat)
        // 50 / 100 = 0.50000000
        assertEquals(BigDecimal("0.50000000"), BigDecimal(vm.state.value.amountString))
    }

    @Test
    fun `toggle LTC to fiat preserves amountInLitoshi`() = runTest {
        val vm = buildViewModel()
        vm.onEvent(SendEvent.OnAmountChanged("1")) // 1 LTC = 100_000_000 litoshis
        advanceUntilIdle()
        val litoshiBefore = vm.state.value.amountInLitoshi

        vm.onEvent(SendEvent.OnToggleFiatOrLTC) // → fiat, litoshi unchanged
        advanceUntilIdle()

        assertEquals(litoshiBefore, vm.state.value.amountInLitoshi)
    }

    @Test
    fun `toggle fiat to LTC updates amountInLitoshi`() = runTest {
        val vm = buildViewModel()
        vm.onEvent(SendEvent.OnToggleFiatOrLTC) // → fiat
        advanceUntilIdle()
        vm.onEvent(SendEvent.OnAmountChanged("100")) // 100 USD / 100 rate = 1 LTC
        advanceUntilIdle()

        vm.onEvent(SendEvent.OnToggleFiatOrLTC) // → LTC
        advanceUntilIdle()

        // 1 LTC = 100_000_000 litoshis
        assertEquals(
            0,
            BigDecimal("100000000").compareTo(vm.state.value.amountInLitoshi)
        )
    }

    // -------------------------------------------------------------------------
    // OnAmountChanged — uses selectedCurrency.rate (Float, always present)
    // -------------------------------------------------------------------------

    @Test
    fun `valid address alone does not enable send`() = runTest {
        val vm = buildViewModel()
        vm.onEvent(SendEvent.OnRecipientAddressChanged("LValidAddr"))
        advanceUntilIdle()

        assertTrue(vm.state.value.isLTCAddressValid)
        assertFalse(
            "Send must stay disabled until an amount is entered too",
            vm.state.value.isReadyToSend
        )
    }

    @Test
    fun `valid address plus valid amount enables send`() = runTest {
        val vm = buildViewModel()
        vm.onEvent(SendEvent.OnRecipientAddressChanged("LValidAddr"))
        vm.onEvent(SendEvent.OnAmountChanged("0.1"))
        advanceUntilIdle()

        assertTrue(vm.state.value.isReadyToSend)
    }

    @Test
    fun `amount exceeding balance disables send`() = runTest {
        val vm = buildViewModel(getBalance = { 1000L })

        vm.onEvent(SendEvent.OnRecipientAddressChanged("LValidAddr"))
        vm.onEvent(SendEvent.OnAmountChanged("100"))
        advanceUntilIdle()

        assertFalse(vm.state.value.isAmountBelowBalance)
        assertFalse(vm.state.value.isReadyToSend)
    }

    @Test
    fun `invalid address disables send even with valid amount`() = runTest {
        val vm = buildViewModel(validateAddress = { false })
        vm.onEvent(SendEvent.OnRecipientAddressChanged("not-a-real-addr"))
        vm.onEvent(SendEvent.OnAmountChanged("0.1"))
        advanceUntilIdle()

        assertFalse(vm.state.value.isLTCAddressValid)
        assertFalse(vm.state.value.isReadyToSend)
    }

    @Test
    fun `zero amount does not enable send`() = runTest {
        val vm = buildViewModel()
        vm.onEvent(SendEvent.OnRecipientAddressChanged("LValidAddr"))
        vm.onEvent(SendEvent.OnAmountChanged("0"))
        advanceUntilIdle()

        assertFalse(
            "Zero-amount transactions must never be sendable",
            vm.state.value.isReadyToSend
        )
    }

    @Test
    fun `empty amount string does not enable send`() = runTest {
        val vm = buildViewModel()
        vm.onEvent(SendEvent.OnRecipientAddressChanged("LValidAddr"))
        vm.onEvent(SendEvent.OnAmountChanged(""))
        advanceUntilIdle()

        assertFalse(vm.state.value.isReadyToSend)
    }

    @Test
    fun `fiat amount converts to litoshis correctly for balance check`() = runTest {
        // 50 USD / 100 rate = 0.5 LTC = 50_000_000 litoshis
        // balance = 1_000_000_000 litoshis (10 LTC) → should be valid
        val vm = buildViewModel()
        vm.onEvent(SendEvent.OnToggleFiatOrLTC) // → fiat
        advanceUntilIdle()
        vm.onEvent(SendEvent.OnRecipientAddressChanged("LValidAddr"))
        vm.onEvent(SendEvent.OnAmountChanged("50"))
        advanceUntilIdle()

        assertTrue(vm.state.value.isAmountBelowBalance)
        assertTrue(vm.state.value.isReadyToSend)
    }

    // -------------------------------------------------------------------------
    // Send result handling — brainwalletIsPublishing always clears
    // -------------------------------------------------------------------------

    @Test
    fun `send success clears publishing flag`() = runTest {
        coEvery { bwSender.prepareTransaction(any()) } returns BWSendResult.Success
        val vm = buildViewModel()

        vm.onEvent(SendEvent.OnSend(dummyTransactionItem()))
        advanceUntilIdle()

        assertFalse(vm.state.value.brainwalletIsPublishing)
    }

    @Test
    fun `send AlreadySending clears publishing flag`() = runTest {
        coEvery { bwSender.prepareTransaction(any()) } returns BWSendResult.Error.AlreadySending
        val vm = buildViewModel()

        vm.onEvent(SendEvent.OnSend(dummyTransactionItem()))
        advanceUntilIdle()

        assertFalse(vm.state.value.brainwalletIsPublishing)
    }

    @Test
    fun `send TimedOut clears publishing flag and surfaces error`() = runTest {
        coEvery { bwSender.prepareTransaction(any()) } returns BWSendResult.Error.TimedOut
        val vm = buildViewModel()

        vm.onEvent(SendEvent.OnSend(dummyTransactionItem()))
        advanceUntilIdle()

        assertFalse(vm.state.value.brainwalletIsPublishing)
        assertTrue(vm.state.value.errorResultString.isNotEmpty())
    }

    @Test
    fun `send Unknown error clears publishing flag and surfaces error`() = runTest {
        coEvery { bwSender.prepareTransaction(any()) } returns
            BWSendResult.Error.Unknown(RuntimeException("boom"))
        val vm = buildViewModel()

        vm.onEvent(SendEvent.OnSend(dummyTransactionItem()))
        advanceUntilIdle()

        assertFalse(vm.state.value.brainwalletIsPublishing)
        assertTrue(vm.state.value.errorResultString.isNotEmpty())
    }

    @Test
    fun `send AmountTooSmall surfaces error and clears publishing`() = runTest {
        coEvery { bwSender.prepareTransaction(any()) } returns
            BWSendResult.Error.AmountTooSmall(minAmount = 10_000L)
        val vm = buildViewModel()

        vm.onEvent(SendEvent.OnSend(dummyTransactionItem()))
        advanceUntilIdle()

        assertFalse(vm.state.value.brainwalletIsPublishing)
        assertTrue(vm.state.value.errorResultString.isNotEmpty())
    }

    @Test
    fun `send InsufficientFunds surfaces error and clears publishing`() = runTest {
        coEvery { bwSender.prepareTransaction(any()) } returns
            BWSendResult.Error.InsufficientFunds
        val vm = buildViewModel()

        vm.onEvent(SendEvent.OnSend(dummyTransactionItem()))
        advanceUntilIdle()

        assertFalse(vm.state.value.brainwalletIsPublishing)
        assertTrue(vm.state.value.errorResultString.isNotEmpty())
    }

    @Test
    fun `onSend calls bwSender exactly once`() = runTest {
        coEvery { bwSender.prepareTransaction(any()) } returns BWSendResult.Success
        val vm = buildViewModel()

        vm.onEvent(SendEvent.OnSend(dummyTransactionItem()))
        advanceUntilIdle()

        coVerify(exactly = 1) { bwSender.prepareTransaction(any()) }
    }

    // -------------------------------------------------------------------------
    // Passcode flow
    // -------------------------------------------------------------------------

    @Test
    fun `correct passcode triggers send`() = runTest {
        coEvery { bwSender.prepareTransaction(any()) } returns BWSendResult.Success
        every { BRKeyStore.getPinCode(any()) } returns "1234"
        val vm = buildViewModel()

        vm.onEvent(SendEvent.OnPasscodeDigitAdded(1))
        vm.onEvent(SendEvent.OnPasscodeDigitAdded(2))
        vm.onEvent(SendEvent.OnPasscodeDigitAdded(3))
        vm.onEvent(SendEvent.OnPasscodeDigitAdded(4))
        advanceUntilIdle()

        coVerify(exactly = 1) { bwSender.prepareTransaction(any()) }
    }

    @Test
    fun `incorrect passcode does not trigger send and flags auth failure`() = runTest {
        every { BRKeyStore.getPinCode(any()) } returns "9999"
        val vm = buildViewModel()

        vm.onEvent(SendEvent.OnPasscodeDigitAdded(1))
        vm.onEvent(SendEvent.OnPasscodeDigitAdded(2))
        vm.onEvent(SendEvent.OnPasscodeDigitAdded(3))
        vm.onEvent(SendEvent.OnPasscodeDigitAdded(4))
        advanceUntilIdle()

        assertFalse(vm.state.value.isPasscodeAuthenticated)
        coVerify(exactly = 0) { bwSender.prepareTransaction(any()) }
    }

    @Test
    fun `passcode caps at four digits`() = runTest {
        val vm = buildViewModel()
        repeat(6) { vm.onEvent(SendEvent.OnPasscodeDigitAdded(1)) }
        advanceUntilIdle()

        assertEquals(4, vm.state.value.passcode.size)
    }

    @Test
    fun `passcode delete removes last digit`() = runTest {
        val vm = buildViewModel()
        vm.onEvent(SendEvent.OnPasscodeDigitAdded(1))
        vm.onEvent(SendEvent.OnPasscodeDigitAdded(2))
        vm.onEvent(SendEvent.OnPasscodeDigitDeleted)
        advanceUntilIdle()

        assertEquals(listOf(1), vm.state.value.passcode)
    }

    // -------------------------------------------------------------------------
    // QR scan / EventBus
    // -------------------------------------------------------------------------

    @Test
    fun `QR scan event populates recipient address`() = runTest {
        val vm = buildViewModel()
        EventBus.emit(EventBus.Event.QRCodeScanned(url = "LQRScannedAddr"))
        advanceUntilIdle()

        assertEquals("LQRScannedAddr", vm.state.value.recipientLTCAddress)
    }

    @Test
    fun `QR scan with null url sets empty string not crash`() = runTest {
        val vm = buildViewModel()
        EventBus.emit(EventBus.Event.QRCodeScanned(url = null))
        advanceUntilIdle()

        assertEquals("", vm.state.value.recipientLTCAddress)
    }

    // -------------------------------------------------------------------------
    // Memo / Edit
    // -------------------------------------------------------------------------

    @Test
    fun `memo update stores value`() = runTest {
        val vm = buildViewModel()
        vm.onEvent(SendEvent.OnUserMemorandumChanged("coffee"))
        advanceUntilIdle()

        assertEquals("coffee", vm.state.value.userMemorandum)
    }

    @Test
    fun `edit send clears amount`() = runTest {
        val vm = buildViewModel()
        vm.onEvent(SendEvent.OnAmountChanged("0.1"))
        advanceUntilIdle()
        vm.onEvent(SendEvent.OnEditSend)
        advanceUntilIdle()

        assertEquals("", vm.state.value.amountString)
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun dummyTransactionItem() = TransactionItem(
        "LValidAddr",
        "LOpsAddr",
        null,
        100_000L,
        500L,
        null,
        false,
        ""
    )
}
