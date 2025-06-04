package com.brainwallet.ui.screens.setpasscode

import com.brainwallet.navigation.Route
import com.brainwallet.navigation.UiEffect
import com.brainwallet.util.EventBus
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockkObject
import io.mockk.slot
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
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.spyk

@ExperimentalCoroutinesApi
class SetPasscodeViewModelTest {

    private lateinit var viewModel: SetPasscodeViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        mockkObject(EventBus)
        coEvery { EventBus.emit(any()) } returns Unit

        viewModel = spyk(SetPasscodeViewModel())
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
        assertEquals(false, initialState.isConfirm)
        assertEquals(listOf(-1, -1, -1, -1), initialState.passcode)
        assertEquals(listOf(-1, -1, -1, -1), initialState.passcodeConfirm)
    }

    @Test
    fun `test OnLoad event with empty passcode`() = runTest {
        viewModel.onEvent(SetPasscodeEvent.OnLoad(listOf()))
        val updatedState = viewModel.state.first()

        assertEquals(false, updatedState.isConfirm)
        assertEquals(listOf(-1, -1, -1, -1), updatedState.passcode)
    }

    @Test
    fun `test OnLoad event with passcode`() = runTest {
        val passcode = listOf(1, 2, 3, 4)
        viewModel.onEvent(SetPasscodeEvent.OnLoad(passcode))
        val updatedState = viewModel.state.first()

        assertEquals(true, updatedState.isConfirm)
        assertEquals(passcode, updatedState.passcode)
    }

    @Test
    fun `test OnDigitChange event in passcode entry mode`() = runTest {
        viewModel.onEvent(SetPasscodeEvent.OnDigitChange(1))
        val state1 = viewModel.state.first()
        assertEquals(listOf(1, -1, -1, -1), state1.passcode)

        viewModel.onEvent(SetPasscodeEvent.OnDigitChange(2))
        val state2 = viewModel.state.first()
        assertEquals(listOf(1, 2, -1, -1), state2.passcode)
    }

    @Test
    fun `test OnDigitChange event with invalid digit`() = runTest {
        val initialState = viewModel.state.first()
        viewModel.onEvent(SetPasscodeEvent.OnDigitChange(-2))
        val updatedState = viewModel.state.first()

        assertEquals(initialState, updatedState)
    }

    @Test
    fun `test OnDeleteDigit event in passcode entry mode`() = runTest {
        // First add some digits
        viewModel.onEvent(SetPasscodeEvent.OnDigitChange(1))
        viewModel.onEvent(SetPasscodeEvent.OnDigitChange(2))
        viewModel.onEvent(SetPasscodeEvent.OnDigitChange(3))

        // Now delete one
        viewModel.onEvent(SetPasscodeEvent.OnDeleteDigit)
        val state = viewModel.state.first()
        assertEquals(listOf(1, 2, -1, -1), state.passcode)

        // Delete another
        viewModel.onEvent(SetPasscodeEvent.OnDeleteDigit)
        val state2 = viewModel.state.first()
        assertEquals(listOf(1, -1, -1, -1), state2.passcode)
    }

    @Test
    fun `test OnDeleteDigit on empty passcode does nothing`() = runTest {
        val initialState = viewModel.state.first()
        viewModel.onEvent(SetPasscodeEvent.OnDeleteDigit)
        val updatedState = viewModel.state.first()

        assertEquals(initialState, updatedState)
    }

    @Test
    fun `test OnDigitChange in confirm mode`() = runTest {
        // Setup confirm mode
        val passcode = listOf(1, 2, 3, 4)
        viewModel.onEvent(SetPasscodeEvent.OnLoad(passcode))

        // Enter confirmation digits
        viewModel.onEvent(SetPasscodeEvent.OnDigitChange(1))
        viewModel.onEvent(SetPasscodeEvent.OnDigitChange(2))

        val state = viewModel.state.first()
        assertEquals(listOf(1, 2, -1, -1), state.passcodeConfirm)
    }

    @Test
    fun `test OnDeleteDigit in confirm mode`() = runTest {
        // Setup confirm mode
        val passcode = listOf(1, 2, 3, 4)
        viewModel.onEvent(SetPasscodeEvent.OnLoad(passcode))

        // Enter confirmation digits
        viewModel.onEvent(SetPasscodeEvent.OnDigitChange(1))
        viewModel.onEvent(SetPasscodeEvent.OnDigitChange(2))

        // Delete one digit
        viewModel.onEvent(SetPasscodeEvent.OnDeleteDigit)

        val state = viewModel.state.first()
        assertEquals(listOf(1, -1, -1, -1), state.passcodeConfirm)
    }

    @Test
    fun `test navigation occurs when passcode is fully entered`() = runTest {
        // Fill the passcode (4 digits)
        for (i in 1..4) {
            viewModel.onEvent(SetPasscodeEvent.OnDigitChange(i))
        }
        
        // Advance the dispatcher to ensure all coroutines are completed
        advanceUntilIdle()

        val uiEffectSlot = slot<UiEffect>()
        verify { viewModel.sendUiEffect(capture(uiEffectSlot)) }

        assertTrue(uiEffectSlot.captured is UiEffect.Navigate)
        val navEffect = uiEffectSlot.captured as UiEffect.Navigate
        assertTrue(navEffect.destinationRoute is Route.SetPasscode)

        val routePasscode = (navEffect.destinationRoute as Route.SetPasscode).passcode
        assertEquals(listOf(1, 2, 3, 4), routePasscode)
    }

    @Test
    fun `test EventBus emits when passcodes match in confirm mode`() = runTest {
        // Setup confirm mode with passcode (4 digits)
        val passcode = listOf(1, 2, 3, 4)
        viewModel.onEvent(SetPasscodeEvent.OnLoad(passcode))

        // Enter matching confirmation
        for (i in 1..4) {
            viewModel.onEvent(SetPasscodeEvent.OnDigitChange(i))
        }

        // Advance the dispatcher to ensure all coroutines are completed
        advanceUntilIdle()

        val eventSlot = slot<EventBus.Event>()
        coVerify { EventBus.emit(capture(eventSlot)) }

        assertTrue(eventSlot.captured is EventBus.Event.LegacyPasscodeVerified)
        val passcodeCaptured =
            (eventSlot.captured as EventBus.Event.LegacyPasscodeVerified).passcode
        assertEquals(passcode, passcodeCaptured)
    }
}
