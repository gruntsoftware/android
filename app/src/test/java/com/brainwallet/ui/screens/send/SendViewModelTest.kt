package com.brainwallet.ui.screens.send

import android.app.Application
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.data.repository.TxRepository
import com.brainwallet.presenter.entities.TransactionItem
import com.brainwallet.tools.manager.AnalyticsManager
import com.brainwallet.tools.security.BRKeyStore
import com.brainwallet.tools.util.Utils
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

/**
 * Regression tests for the Send flow. These cover the state transitions that,
 * if broken, would leave the Send button disabled or cause a crash before a
 * transaction could be submitted — matching the production incident.
 *
 * JNI-backed methods (BRWalletManager.validateAddress, getBalance, FeeManager,
 * Utils.tieredOpsFee) are injected as lambdas so these tests never trigger the
 * native linker.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SendViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var app: Application
    private lateinit var bwSender: BWSender
    private lateinit var txRepository: TxRepository
    private lateinit var settingRepository: SettingRepository

    private val settingsFlow = MutableStateFlow(
        AppSetting(
            isDarkMode = false,
            currency = CurrencyEntity("USD", "US Dollar", 100f, "$")
        )
    )

    /**
     * Build a ViewModel with sensible test defaults. Individual tests override
     * whatever they need to exercise specific branches.
     */

    private fun TestScope.buildViewModel(
        getBalance: () -> Long = { 1_000_000_000L }, // 10 LTC in litoshis
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

        mockkStatic(BRKeyStore::class)
        every { BRKeyStore.getPinCode(any()) } returns "1234"

        // Stub the asset/Firebase boundary that OnSend hits.
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

        settingsFlow.value = AppSetting(isDarkMode = true, currency = newCurrency)
        advanceUntilIdle()

        assertTrue(vm.state.value.darkMode)
        assertEquals("GBP", vm.state.value.selectedCurrency.code)
    }

    // -------------------------------------------------------------------------
    // REGRESSION: OnToggleFiatOrLTC null / negative rate handling
    // -------------------------------------------------------------------------

    @Test
    fun `toggle does not crash when rate is negative sentinel`() = runTest {
        // The default CurrencyEntity in SendState has rate = -1f. If the user
        // toggles before rates load, the VM must not crash.
        settingsFlow.value = AppSetting(
            currency = CurrencyEntity("USD", "US Dollar", -1f, "$")
        )
        val vm = buildViewModel()

        vm.onEvent(SendEvent.OnAmountChanged("0.5"))
        advanceUntilIdle()
        vm.onEvent(SendEvent.OnToggleFiatOrLTC)
        advanceUntilIdle()

        // Expectation: no crash. The amount string should either stay put or
        // clear — but the VM must remain functional.
        assertFalse(vm.state.value.brainwalletIsPublishing)
    }

    @Test
    fun `toggle LTC to fiat multiplies by rate with 2dp rounding`() = runTest {
        settingsFlow.value = AppSetting(
            currency = CurrencyEntity("USD", "US Dollar", 100f, "$")
        )
        val vm = buildViewModel()
        // Start in LTC view, enter 0.5 LTC.
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
        settingsFlow.value = AppSetting(
            currency = CurrencyEntity("USD", "US Dollar", 100f, "$")
        )
        val vm = buildViewModel()
        vm.onEvent(SendEvent.OnToggleFiatOrLTC) // → fiat view
        vm.onEvent(SendEvent.OnAmountChanged("50"))
        advanceUntilIdle()

        vm.onEvent(SendEvent.OnToggleFiatOrLTC) // → back to LTC
        advanceUntilIdle()

        assertFalse(vm.state.value.userViewsFiat)
        // 50 / 100 = 0.50000000
        assertEquals(BigDecimal("0.50000000"), BigDecimal(vm.state.value.amountString))
    }

    // -------------------------------------------------------------------------
    // REGRESSION: Amount validation drives isReadyToSend
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

        // Note: this asserts the intended contract. It will currently fail
        // unless the getBalance/getCurrentFee/getOpsFee lambdas are wired into
        // the ViewModel — which is exactly the point of this regression test.
        assertTrue(vm.state.value.isReadyToSend)
    }

    @Test
    fun `amount exceeding balance disables send`() = runTest {
        val vm = buildViewModel(getBalance = { 1000L }) // 1000 litoshis ~ tiny

        vm.onEvent(SendEvent.OnRecipientAddressChanged("LValidAddr"))
        vm.onEvent(SendEvent.OnAmountChanged("100")) // 100 LTC — way over
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

    // -------------------------------------------------------------------------
    // REGRESSION: Send button lock-up (isSending never clears)
    // -------------------------------------------------------------------------

    @Test
    fun `send success clears publishing flag`() = runTest {
        coEvery { bwSender.prepareTransaction(any()) } returns BWSendResult.Success
        val vm = buildViewModel()

        vm.onEvent(SendEvent.OnSend(dummyTransactionItem()))
        advanceUntilIdle()

        assertFalse(
            "brainwalletIsPublishing must be cleared after success",
            vm.state.value.brainwalletIsPublishing
        )
    }

    @Test
    fun `send AlreadySending clears publishing flag`() = runTest {
        // This is the core regression: if AlreadySending leaves the VM in
        // brainwalletIsPublishing = true, the Send button never re-enables
        // and the user is permanently blocked.
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
    // QR scan / EventBus wiring
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
