package com.brainwallet.ui.screens.reenterpin

sealed class ReEnterPinIntent {
    data class DigitPressed(val digit: Int) : ReEnterPinIntent()
    object DeletePressed : ReEnterPinIntent()
    object ValidatePin : ReEnterPinIntent()
    object ClearError : ReEnterPinIntent()
    data class Initialize(val originalPin: String?) : ReEnterPinIntent()
}
