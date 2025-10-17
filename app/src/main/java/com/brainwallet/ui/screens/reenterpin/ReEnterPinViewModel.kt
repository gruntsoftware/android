package com.brainwallet.ui.screens.reenterpin

import com.grunt.brainwallet.core.presentation.util.BaseViewModel
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class ReEnterPinViewModel : BaseViewModel<ReEnterPinState, ReEnterPinSideEffect>(
    initialState = ReEnterPinState()
) {

    fun handleIntent(intent: ReEnterPinIntent) = when (intent) {
        is ReEnterPinIntent.Initialize -> handleInitialize(intent.originalPin)
        is ReEnterPinIntent.DigitPressed -> handleDigitPressed(intent.digit)
        ReEnterPinIntent.DeletePressed -> handleDeletePressed()
        ReEnterPinIntent.ValidatePin -> handleValidatePin()
        ReEnterPinIntent.ClearError -> handleClearError()
    }

    private fun handleInitialize(originalPin: String?) = intent {
        when {
            originalPin.isNullOrEmpty() -> {
                postSideEffect(ReEnterPinSideEffect.ShowError("Invalid PIN data"))
                postSideEffect(ReEnterPinSideEffect.NavigateBack)
            }
            originalPin.length != PIN_LENGTH -> {
                postSideEffect(ReEnterPinSideEffect.ShowError("Invalid PIN format"))
                postSideEffect(ReEnterPinSideEffect.NavigateBack)
            }
            !originalPin.all { it.isDigit() } -> {
                postSideEffect(ReEnterPinSideEffect.ShowError("Invalid PIN format"))
                postSideEffect(ReEnterPinSideEffect.NavigateBack)
            }
            else -> {
                reduce {
                    state.copy(
                        originalPin = originalPin,
                        error = null,
                        showValidationError = false
                    )
                }
            }
        }
    }

    private fun handleDigitPressed(digit: Int) = intent {
        if (digit !in 0..9) {
            return@intent
        }

        if (state.currentPin.length >= PIN_LENGTH) {
            return@intent
        }

        val newPin = state.currentPin + digit.toString()

        reduce {
            state.copy(
                currentPin = newPin,
                isPinComplete = newPin.length == PIN_LENGTH,
                error = null,
                showValidationError = false
            )
        }

        if (newPin.length == PIN_LENGTH) {
            handleValidatePin()
        }
    }

    private fun handleDeletePressed() = intent {
        if (state.currentPin.isEmpty()) {
            return@intent
        }

        val newPin = state.currentPin.dropLast(1)

        reduce {
            state.copy(
                currentPin = newPin,
                isPinComplete = false,
                error = null,
                showValidationError = false
            )
        }
    }

    private fun handleValidatePin() = intent {
        if (state.originalPin.isEmpty()) {
            postSideEffect(ReEnterPinSideEffect.ShowError("PIN validation error"))
            postSideEffect(ReEnterPinSideEffect.NavigateBack)
            return@intent
        }

        if (state.currentPin.length != PIN_LENGTH) {
            return@intent
        }

        reduce { state.copy(isLoading = true) }

        val isValid = state.currentPin == state.originalPin

        if (isValid) {
            reduce {
                state.copy(
                    isLoading = false,
                    error = null,
                    showValidationError = false
                )
            }
            postSideEffect(ReEnterPinSideEffect.TriggerHapticFeedback)
            postSideEffect(ReEnterPinSideEffect.NavigateToSuccess)
        } else {
            reduce {
                state.copy(
                    isLoading = false,
                    currentPin = "",
                    isPinComplete = false,
                    error = "PINs do not match. Please try again.",
                    showValidationError = true
                )
            }
            postSideEffect(ReEnterPinSideEffect.TriggerHapticFeedback)
            postSideEffect(ReEnterPinSideEffect.PlayErrorAnimation)
        }
    }

    private fun handleClearError() = intent {
        reduce {
            state.copy(
                error = null,
                showValidationError = false
            )
        }
    }

    companion object {
        private const val TAG = "ReEnterPinViewModel"
        private const val PIN_LENGTH = 4
    }
}
