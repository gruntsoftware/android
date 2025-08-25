package com.brainwallet.ui.screens.unlock

sealed class UnLockEvent {
    data class OnLoad(
        val isUpdatePin: Boolean = false,
    ) : UnLockEvent()
    data class OnPinDigitChange(
        val digit: Int,
        val isValidPin: (String) -> Boolean
    ) : UnLockEvent()
    object OnToggleDarkMode : UnLockEvent()
    object OnQrClicked : UnLockEvent()
    object OnDeletePinDigit : UnLockEvent()
}

//todo