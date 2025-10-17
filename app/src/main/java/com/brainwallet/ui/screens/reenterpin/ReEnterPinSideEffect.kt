package com.brainwallet.ui.screens.reenterpin

sealed class ReEnterPinSideEffect {
    object NavigateBack : ReEnterPinSideEffect()
    object NavigateToSuccess : ReEnterPinSideEffect()
    data class ShowError(val message: String) : ReEnterPinSideEffect()
    object TriggerHapticFeedback : ReEnterPinSideEffect()
    object PlayErrorAnimation : ReEnterPinSideEffect()
}
