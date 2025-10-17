package com.brainwallet.ui.screens.reenterpin

import com.brainwallet.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.orbitmvi.orbit.test.test

@OptIn(ExperimentalCoroutinesApi::class)
class ReEnterPinViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: ReEnterPinViewModel

    @Before
    fun setUp() {
        viewModel = ReEnterPinViewModel()
    }

    @Test
    fun `when initialized with valid PIN then state contains original PIN`() = runTest {
        val originalPin = "1234"

        viewModel.test(this, initialState = ReEnterPinState()) {
            viewModel.handleIntent(ReEnterPinIntent.Initialize(originalPin))

            expectState {
                ReEnterPinState(originalPin = originalPin)
            }
        }
    }

    @Test
    fun `when initialized with null PIN then shows error and navigates back`() = runTest {
        viewModel.test(this, initialState = ReEnterPinState()) {
            viewModel.handleIntent(ReEnterPinIntent.Initialize(null))

            expectSideEffect(ReEnterPinSideEffect.ShowError("Invalid PIN data"))
            expectSideEffect(ReEnterPinSideEffect.NavigateBack)
        }
    }

    @Test
    fun `when initialized with empty PIN then shows error and navigates back`() = runTest {
        viewModel.test(this, initialState = ReEnterPinState()) {
            viewModel.handleIntent(ReEnterPinIntent.Initialize(""))

            expectSideEffect(ReEnterPinSideEffect.ShowError("Invalid PIN data"))
            expectSideEffect(ReEnterPinSideEffect.NavigateBack)
        }
    }

    @Test
    fun `when initialized with invalid PIN length then shows error and navigates back`() = runTest {
        viewModel.test(this, initialState = ReEnterPinState()) {
            viewModel.handleIntent(ReEnterPinIntent.Initialize("123"))

            expectSideEffect(ReEnterPinSideEffect.ShowError("Invalid PIN format"))
            expectSideEffect(ReEnterPinSideEffect.NavigateBack)
        }
    }

    @Test
    fun `when initialized with non-digit characters then shows error and navigates back`() = runTest {
        viewModel.test(this, initialState = ReEnterPinState()) {
            viewModel.handleIntent(ReEnterPinIntent.Initialize("12a4"))

            expectSideEffect(ReEnterPinSideEffect.ShowError("Invalid PIN format"))
            expectSideEffect(ReEnterPinSideEffect.NavigateBack)
        }
    }

    @Test
    fun `given empty PIN when multiple digits pressed then PIN builds sequentially`() = runTest {
        val initialState = ReEnterPinState(originalPin = "1234")

        viewModel.test(this, initialState = initialState) {
            viewModel.handleIntent(ReEnterPinIntent.DigitPressed(1))
            expectState {
                initialState.copy(
                    currentPin = "1",
                    isPinComplete = false,
                    error = null,
                    showValidationError = false
                )
            }

            viewModel.handleIntent(ReEnterPinIntent.DigitPressed(2))
            expectState {
                initialState.copy(
                    currentPin = "12",
                    isPinComplete = false,
                    error = null,
                    showValidationError = false
                )
            }

            viewModel.handleIntent(ReEnterPinIntent.DigitPressed(3))
            expectState {
                initialState.copy(
                    currentPin = "123",
                    isPinComplete = false,
                    error = null,
                    showValidationError = false
                )
            }
        }
    }

    @Test
    fun `given three digits entered when correct fourth digit pressed then validates automatically and navigates to success`() = runTest {
        val originalPin = "1234"
        val initialState = ReEnterPinState(
            originalPin = originalPin,
            currentPin = "123"
        )

        viewModel.test(this, initialState = initialState) {
            viewModel.handleIntent(ReEnterPinIntent.DigitPressed(4))

            expectState {
                initialState.copy(
                    currentPin = "1234",
                    isPinComplete = true,
                    error = null,
                    showValidationError = false
                )
            }

            expectState {
                initialState.copy(
                    currentPin = "1234",
                    isPinComplete = true,
                    isLoading = true,
                    error = null,
                    showValidationError = false
                )
            }

            expectState {
                initialState.copy(
                    currentPin = "1234",
                    isPinComplete = true,
                    isLoading = false,
                    error = null,
                    showValidationError = false
                )
            }

            expectSideEffect(ReEnterPinSideEffect.TriggerHapticFeedback)
            expectSideEffect(ReEnterPinSideEffect.NavigateToSuccess)
        }
    }

    @Test
    fun `given three digits entered when incorrect fourth digit pressed then shows error and clears PIN`() = runTest {
        val originalPin = "1234"
        val initialState = ReEnterPinState(
            originalPin = originalPin,
            currentPin = "567"
        )

        viewModel.test(this, initialState = initialState) {
            viewModel.handleIntent(ReEnterPinIntent.DigitPressed(8))

            expectState {
                initialState.copy(
                    currentPin = "5678",
                    isPinComplete = true,
                    error = null,
                    showValidationError = false
                )
            }

            expectState {
                initialState.copy(
                    currentPin = "5678",
                    isPinComplete = true,
                    isLoading = true,
                    error = null,
                    showValidationError = false
                )
            }

            expectState {
                ReEnterPinState(
                    originalPin = originalPin,
                    currentPin = "",
                    isPinComplete = false,
                    isLoading = false,
                    error = "PINs do not match. Please try again.",
                    showValidationError = true
                )
            }

            expectSideEffect(ReEnterPinSideEffect.TriggerHapticFeedback)
            expectSideEffect(ReEnterPinSideEffect.PlayErrorAnimation)
        }
    }

    @Test
    fun `given partial PIN when invalid digit pressed then PIN remains unchanged`() = runTest {
        val initialState = ReEnterPinState(originalPin = "1234", currentPin = "12")

        viewModel.test(this, initialState = initialState) {
            viewModel.handleIntent(ReEnterPinIntent.DigitPressed(-1))
        }
    }

    @Test
    fun `given complete PIN when digit pressed then ignores input`() = runTest {
        val initialState = ReEnterPinState(
            originalPin = "1234",
            currentPin = "5678",
            isPinComplete = true
        )

        viewModel.test(this, initialState = initialState) {
            viewModel.handleIntent(ReEnterPinIntent.DigitPressed(9))
        }
    }

    @Test
    fun `given partial PIN when delete pressed then removes last digit`() = runTest {
        val initialState = ReEnterPinState(
            originalPin = "1234",
            currentPin = "567"
        )

        viewModel.test(this, initialState = initialState) {
            viewModel.handleIntent(ReEnterPinIntent.DeletePressed)

            expectState {
                initialState.copy(
                    currentPin = "56",
                    isPinComplete = false,
                    error = null,
                    showValidationError = false
                )
            }
        }
    }

    @Test
    fun `given empty PIN when delete pressed then ignores input`() = runTest {
        val initialState = ReEnterPinState(originalPin = "1234", currentPin = "")

        viewModel.test(this, initialState = initialState) {
            viewModel.handleIntent(ReEnterPinIntent.DeletePressed)
        }
    }

    @Test
    fun `when validate PIN called with matching PIN then navigates to success`() = runTest {
        val originalPin = "1234"
        val initialState = ReEnterPinState(
            originalPin = originalPin,
            currentPin = originalPin,
            isPinComplete = true
        )

        viewModel.test(this, initialState = initialState) {
            viewModel.handleIntent(ReEnterPinIntent.ValidatePin)

            expectState {
                initialState.copy(isLoading = true)
            }

            expectState {
                initialState.copy(
                    isLoading = false,
                    error = null,
                    showValidationError = false
                )
            }

            expectSideEffect(ReEnterPinSideEffect.TriggerHapticFeedback)
            expectSideEffect(ReEnterPinSideEffect.NavigateToSuccess)
        }
    }

    @Test
    fun `when validate PIN called with non-matching PIN then shows error`() = runTest {
        val initialState = ReEnterPinState(
            originalPin = "1234",
            currentPin = "5678",
            isPinComplete = true
        )

        viewModel.test(this, initialState = initialState) {
            viewModel.handleIntent(ReEnterPinIntent.ValidatePin)

            expectState {
                initialState.copy(isLoading = true)
            }

            expectState {
                ReEnterPinState(
                    originalPin = "1234",
                    currentPin = "",
                    isPinComplete = false,
                    isLoading = false,
                    error = "PINs do not match. Please try again.",
                    showValidationError = true
                )
            }

            expectSideEffect(ReEnterPinSideEffect.TriggerHapticFeedback)
            expectSideEffect(ReEnterPinSideEffect.PlayErrorAnimation)
        }
    }

    @Test
    fun `when validate PIN called without original PIN then shows error and navigates back`() = runTest {
        val initialState = ReEnterPinState(
            originalPin = "",
            currentPin = "1234",
            isPinComplete = true
        )

        viewModel.test(this, initialState = initialState) {
            viewModel.handleIntent(ReEnterPinIntent.ValidatePin)

            expectSideEffect(ReEnterPinSideEffect.ShowError("PIN validation error"))
            expectSideEffect(ReEnterPinSideEffect.NavigateBack)
        }
    }

    @Test
    fun `given incomplete PIN when validate called then ignores validation`() = runTest {
        val initialState = ReEnterPinState(
            originalPin = "1234",
            currentPin = "12",
            isPinComplete = false
        )

        viewModel.test(this, initialState = initialState) {
            viewModel.handleIntent(ReEnterPinIntent.ValidatePin)
        }
    }

    @Test
    fun `when clear error called then removes error state`() = runTest {
        val initialState = ReEnterPinState(
            originalPin = "1234",
            error = "Some error",
            showValidationError = true
        )

        viewModel.test(this, initialState = initialState) {
            viewModel.handleIntent(ReEnterPinIntent.ClearError)

            expectState {
                initialState.copy(
                    error = null,
                    showValidationError = false
                )
            }
        }
    }
}
