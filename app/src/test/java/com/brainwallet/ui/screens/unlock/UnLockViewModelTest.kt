package com.brainwallet.ui.screens.unlock

import android.content.Context
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.navigation.Route
import com.brainwallet.navigation.UiEffect
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.sqlite.CurrencyDataSource
import com.brainwallet.tools.util.BRCurrency
import com.brainwallet.util.EventBus
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.spyk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.math.BigDecimal

@ExperimentalCoroutinesApi
class UnLockViewModelTest {

    private lateinit var viewModel: UnLockViewModel
    private lateinit var context: Context
    private lateinit var currencyDataSource: CurrencyDataSource
    private val dummyCurrency = CurrencyEntity(
        code = "USD",
        name = "US Dollar",
        rate = 1000.0F,
        symbol = "USD"
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        // Mock dependencies
        mockkObject(EventBus)
        mockkStatic(BRSharedPrefs::class)
        mockkStatic(BRCurrency::class)

        context = mockk(relaxed = true)
        currencyDataSource = mockk(relaxed = true)

        coEvery { EventBus.emit(any()) } returns Unit

        // Set up the ViewModel with mocks
        viewModel = spyk(UnLockViewModel(currencyDataSource))
        every { viewModel.sendUiEffect(any()) } just Runs
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        unmockkAll()
    }

    @Test
    fun `test initial state`() = runTest {
        val initialState = viewModel.state.first()
        assertEquals(listOf(-1, -1, -1, -1), initialState.passcode)
        assertEquals("USD", initialState.iso)
        assertEquals("", initialState.formattedCurrency)
        assertEquals(false, initialState.isUpdatePin)
    }

    @Test
    fun `test OnPinDigitChange event adds digit to passcode`() = runTest {
        viewModel.onEvent(UnLockEvent.OnPinDigitChange(1) { _ -> false })
        val state1 = viewModel.state.first()
        assertEquals(listOf(1, -1, -1, -1), state1.passcode)

        viewModel.onEvent(UnLockEvent.OnPinDigitChange(2) { _ -> false })
        val state2 = viewModel.state.first()
        assertEquals(listOf(1, 2, -1, -1), state2.passcode)
    }

    @Test
    fun `test OnPinDigitChange with invalid digit does nothing`() = runTest {
        val initialState = viewModel.state.first()
        viewModel.onEvent(UnLockEvent.OnPinDigitChange(-2) { _ -> false })
        val updatedState = viewModel.state.first()

        assertEquals(initialState, updatedState)
    }

    @Test
    fun `test OnDeletePinDigit removes last non-empty digit`() = runTest {
        // First add some digits
        viewModel.onEvent(UnLockEvent.OnPinDigitChange(1) { _ -> false })
        viewModel.onEvent(UnLockEvent.OnPinDigitChange(2) { _ -> false })

        // Then delete one
        viewModel.onEvent(UnLockEvent.OnDeletePinDigit)
        val state = viewModel.state.first()
        assertEquals(listOf(1, -1, -1, -1), state.passcode)

        // Delete another
        viewModel.onEvent(UnLockEvent.OnDeletePinDigit)
        val emptyState = viewModel.state.first()
        assertEquals(listOf(-1, -1, -1, -1), emptyState.passcode)
    }

    @Test
    fun `test OnDeletePinDigit on empty passcode does nothing`() = runTest {
        val initialState = viewModel.state.first()
        viewModel.onEvent(UnLockEvent.OnDeletePinDigit)
        val updatedState = viewModel.state.first()

        assertEquals(initialState, updatedState)
    }

    @Test
    fun `test OnLoad event updates state with currency information`() = runTest {
        val isoSymbol = "USD"
        val formattedCurrency = "$1,000.00"

        every { BRSharedPrefs.getIsoSymbol(context) } returns isoSymbol
        every { currencyDataSource.getCurrencyByIso(isoSymbol) } returns dummyCurrency
        every {
            BRCurrency.getFormattedCurrencyString(context, isoSymbol, any<BigDecimal>())
        } returns formattedCurrency

        viewModel.onEvent(UnLockEvent.OnLoad(context, false))

        val state = viewModel.state.first()
        assertEquals(isoSymbol, state.iso)
        assertEquals(formattedCurrency, state.formattedCurrency)
        assertEquals(false, state.isUpdatePin)
    }

    @Test
    fun `test filled passcode emits LegacyUnLock event`() = runTest {
        // Fill the passcode
        for (i in 1..4) {
            viewModel.onEvent(UnLockEvent.OnPinDigitChange(i) { _ -> false })
        }

        // Advance the dispatcher to ensure all coroutines are completed
        advanceUntilIdle()

        val eventSlot = slot<EventBus.Event>()
        coVerify { EventBus.emit(capture(eventSlot)) }

        assertTrue(eventSlot.captured is EventBus.Event.LegacyUnLock)
        val passcodeCaptured = (eventSlot.captured as EventBus.Event.LegacyUnLock).passcode
        assertEquals(listOf(1, 2, 3, 4), passcodeCaptured)
    }

    @Test
    fun `test filled passcode with isUpdatePin true navigates to SetPasscode`() = runTest {
        // Set isUpdatePin to true
        every { BRSharedPrefs.getIsoSymbol(context) } returns "USD"
        every { currencyDataSource.getCurrencyByIso("USD") } returns dummyCurrency
        every {
            BRCurrency.getFormattedCurrencyString(context, "USD", any<BigDecimal>())
        } returns "$1,000.00"

        viewModel.onEvent(UnLockEvent.OnLoad(context, true))

        // Fill the passcode with valid pin
        for (i in 1..4) {
            viewModel.onEvent(UnLockEvent.OnPinDigitChange(i) { _ -> true })
        }

        advanceUntilIdle()

        val uiEffectSlot = slot<UiEffect>()
        verify { viewModel.sendUiEffect(capture(uiEffectSlot)) }

        assertTrue(uiEffectSlot.captured is UiEffect.Navigate)
        val navEffect = uiEffectSlot.captured as UiEffect.Navigate
        assertTrue(navEffect.destinationRoute is Route.SetPasscode)
    }
}
