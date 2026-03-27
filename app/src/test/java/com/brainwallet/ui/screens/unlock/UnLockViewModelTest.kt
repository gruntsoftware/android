package com.brainwallet.ui.screens.unlock

import app.cash.turbine.test
import app.cash.turbine.testIn
import app.cash.turbine.turbineScope
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.navigation.Route
import com.brainwallet.navigation.UiEffect
import com.brainwallet.util.CurrencyDataGetter
import com.brainwallet.util.EventBus
import com.brainwallet.util.MainDispatcherRule
import com.brainwallet.util.VersionCodeProvider
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.mockkObject
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UnLockViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @MockK
    private lateinit var mockVersionCodeProvider: VersionCodeProvider

    @MockK
    private lateinit var mockSettingRepository: SettingRepository

    @RelaxedMockK
    private lateinit var mockCurrencyDataGetter: CurrencyDataGetter

    private lateinit var viewModel: UnLockViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this, relaxUnitFun = true)
        every { mockVersionCodeProvider.getFormatted() } returns "v4.7.1"

        viewModel = UnLockViewModel(
            versionCodeProvider = mockVersionCodeProvider,
            settingRepository = mockSettingRepository,
            currencyDataGetter = mockCurrencyDataGetter
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `given viewModel initialization when created then state contains formatted version`() =
        runTest {
            viewModel.state.test {
                val initialState = awaitItem()

                assert(initialState.formattedVersion == "v4.7.1") {
                    "Expected formatted version to be 'v4.7.1' but was '${initialState.formattedVersion}'"
                }
                assert(initialState.passcode == List(4) { -1 }) {
                    "Expected passcode to be empty list of -1 values but was '${initialState.passcode}'"
                }
                assert(initialState.iso == "USD") {
                    "Expected default ISO to be 'USD' but was '${initialState.iso}'"
                }
                assert(!initialState.isUpdatePin) {
                    "Expected isUpdatePin to be false but was '${initialState.isUpdatePin}'"
                }
            }
        }

    @Test
    fun `given valid pin digit when OnPinDigitChange event then passcode updates correctly`() =
        runTest {
            viewModel.state.test {
                awaitItem() // Skip initial state

                viewModel.onEvent(UnLockEvent.OnPinDigitChange(digit = 5, isValidPin = { false }))

                val updatedState = awaitItem()
                assert(updatedState.passcode[0] == 5) {
                    "Expected first digit to be 5 but was '${updatedState.passcode[0]}'"
                }
                assert(updatedState.passcode.drop(1).all { it == -1 }) {
                    "Expected remaining digits to be -1 but were '${updatedState.passcode.drop(1)}'"
                }
            }
        }

    @Test
    fun `given multiple pin digits when OnPinDigitChange events then passcode fills sequentially`() =
        runTest {
            viewModel.state.test {
                awaitItem()

                viewModel.onEvent(UnLockEvent.OnPinDigitChange(digit = 1, isValidPin = { false }))
                awaitItem()

                viewModel.onEvent(UnLockEvent.OnPinDigitChange(digit = 2, isValidPin = { false }))
                awaitItem()

                viewModel.onEvent(UnLockEvent.OnPinDigitChange(digit = 3, isValidPin = { false }))
                val thirdDigitState = awaitItem()

                assert(thirdDigitState.passcode == listOf(1, 2, 3, -1)) {
                    "Expected passcode to be [1, 2, 3, -1] but was '${thirdDigitState.passcode}'"
                }
            }
        }

    @Test
    fun `given invalid digit when OnPinDigitChange with digit less than -1 then passcode remains unchanged`() =
        runTest {
            viewModel.state.test {
                val initialState = awaitItem()

                viewModel.onEvent(UnLockEvent.OnPinDigitChange(digit = -5, isValidPin = { false }))

                expectNoEvents()
                assert(initialState.passcode == List(4) { -1 }) {
                    "Expected passcode to remain unchanged but was modified"
                }
            }
        }

    @Test
    fun `given full passcode when OnPinDigitChange then no additional digits accepted`() = runTest {
        viewModel.state.test {
            awaitItem()

            repeat(4) { index ->
                viewModel.onEvent(
                    UnLockEvent.OnPinDigitChange(
                        digit = index + 1,
                        isValidPin = { false }
                    )
                )
                awaitItem()
            }

            viewModel.onEvent(UnLockEvent.OnPinDigitChange(digit = 9, isValidPin = { false }))

            expectNoEvents()
        }
    }

    @Test
    fun `given filled passcode in update mode with valid pin when OnPinDigitChange then navigates to SetPasscode`() =
        runTest {
            mockkObject(EventBus)

            turbineScope {
                val state = viewModel.state.testIn(backgroundScope)
                val effect = viewModel.uiEffect.testIn(backgroundScope)

                state.awaitItem() // initial state

                viewModel.onEvent(UnLockEvent.OnLoad(isUpdatePin = true))
                state.awaitItem()

                repeat(3) { index ->
                    viewModel.onEvent(
                        UnLockEvent.OnPinDigitChange(
                            digit = index + 1,
                            isValidPin = { true }
                        )
                    )
                    state.awaitItem()
                }

                viewModel.onEvent(
                    UnLockEvent.OnPinDigitChange(
                        digit = 4,
                        isValidPin = { true }
                    )
                )
                state.awaitItem()

                val navigateEffect = effect.awaitItem()
                assert(navigateEffect is UiEffect.Navigate) {
                    "Expected Navigate effect but was '${navigateEffect::class.simpleName}'"
                }
                assert((navigateEffect as UiEffect.Navigate).destinationRoute is Route.SetPasscode) {
                    "Expected SetPasscode but was '${navigateEffect.destinationRoute!!::class.simpleName}'"
                }
            }
        }

    @Test
    fun `given filled passcode in normal mode when OnPinDigitChange then emits LegacyUnLock event`() =
        runTest {
            mockkObject(EventBus)
            coEvery { EventBus.emit(any()) } returns Unit

            viewModel.state.test {
                awaitItem()

                repeat(3) { index ->
                    viewModel.onEvent(
                        UnLockEvent.OnPinDigitChange(
                            digit = index + 1,
                            isValidPin = { false }
                        )
                    )
                    awaitItem()
                }

                viewModel.onEvent(UnLockEvent.OnPinDigitChange(digit = 4, isValidPin = { false }))
                awaitItem()
            }

            testScheduler.advanceUntilIdle()

            coVerify {
                EventBus.emit(
                    match<EventBus.Event.LegacyUnLock> { event ->
                        event.passcode == listOf(1, 2, 3, 4)
                    }
                )
            }
        }

    @Test
    fun `given passcode with digits when OnDeletePinDigit then removes last entered digit`() =
        runTest {
            viewModel.state.test {
                awaitItem()

                viewModel.onEvent(UnLockEvent.OnPinDigitChange(digit = 7, isValidPin = { false }))
                awaitItem()
                viewModel.onEvent(UnLockEvent.OnPinDigitChange(digit = 8, isValidPin = { false }))
                awaitItem()

                viewModel.onEvent(UnLockEvent.OnDeletePinDigit)
                val stateAfterDelete = awaitItem()

                assert(stateAfterDelete.passcode == listOf(7, -1, -1, -1)) {
                    "Expected passcode to be [7, -1, -1, -1] after deletion but was '${stateAfterDelete.passcode}'"
                }
            }
        }

    @Test
    fun `given empty passcode when OnDeletePinDigit then passcode remains unchanged`() = runTest {
        viewModel.state.test {
            val initialState = awaitItem()

            viewModel.onEvent(UnLockEvent.OnDeletePinDigit)

            expectNoEvents()
            assert(initialState.passcode == List(4) { -1 }) {
                "Expected empty passcode to remain unchanged after delete"
            }
        }
    }

    @Test
    fun `given OnLoad event with context when processed then updates state with currency information`() =
        runTest {
            val mockCurrency = CurrencyEntity(rate = 1.2f)

            every { mockCurrencyDataGetter.getIsoSymbol() } returns "EUR"
            every { mockCurrencyDataGetter.getCurrencyByIso("EUR") } returns mockCurrency
            every {
                mockCurrencyDataGetter.getFormattedCurrencyString(
                    "EUR",
                    any()
                )
            } returns "€1.20"

            viewModel.state.test {
                awaitItem()

                viewModel.onEvent(UnLockEvent.OnLoad(isUpdatePin = false))

                val updatedState = awaitItem()
                assert(updatedState.iso == "EUR") {
                    "Expected ISO to be 'EUR' but was '${updatedState.iso}'"
                }
                assert(updatedState.formattedCurrency == "€1.20") {
                    "Expected formatted currency to be '€1.20' but was '${updatedState.formattedCurrency}'"
                }
                assert(updatedState.isUpdatePin == false) {
                    "Expected isUpdatePin to be false but was '${updatedState.isUpdatePin}'"
                }
            }
        }

    @Test
    fun `given OnLoad event with null currency when processed then state remains unchanged`() =
        runTest {
            every { mockCurrencyDataGetter.getIsoSymbol() } returns "INVALID"
            every { mockCurrencyDataGetter.getCurrencyByIso("INVALID") } returns null

            viewModel.state.test {
                val initialState = awaitItem()

                viewModel.onEvent(UnLockEvent.OnLoad(isUpdatePin = false))

                expectNoEvents()
                assert(initialState.iso == "USD") {
                    "Expected ISO to remain 'USD' when currency is null"
                }
            }
        }

    @Test
    fun `given OnToggleDarkMode event when settings exist then toggles dark mode setting`() =
        runTest {
            val mockSettings = AppSetting(isDarkMode = false)

            coEvery { mockSettingRepository.settings } returns flowOf(mockSettings)
            coEvery { mockSettingRepository.save(any()) } returns Unit

            viewModel.onEvent(UnLockEvent.OnToggleDarkMode)
            testScheduler.advanceUntilIdle()

            coVerify {
                mockSettingRepository.save(match { it.isDarkMode })
            }
        }

    @Test
    fun `given OnQrClicked event when processed then sends ShowMoonPayDialog effect`() = runTest {
        turbineScope {
            val effect = viewModel.uiEffect.testIn(backgroundScope)

            viewModel.onEvent(UnLockEvent.OnQrClicked)

            val item = effect.awaitItem()
            assert(item is UiEffect.ShowMoonPayDialog) {
                "Expected ShowMoonPayDialog effect but was '${item::class.simpleName}'"
            }
        }
    }

    @Test
    fun `given isPasscodeFilled extension when all digits are filled then returns true`() {
        val filledState = UnLockState(passcode = listOf(1, 2, 3, 4))
        assert(filledState.isPasscodeFilled()) {
            "Expected isPasscodeFilled to return true for filled passcode"
        }
    }

    @Test
    fun `given isPasscodeFilled extension when passcode has empty digits then returns false`() {
        val partialState = UnLockState(passcode = listOf(1, 2, -1, -1))
        assert(!partialState.isPasscodeFilled()) {
            "Expected isPasscodeFilled to return false for partially filled passcode"
        }
    }
}
