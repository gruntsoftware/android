package com.brainwallet.ui.screen.unlock

sealed class UnLockEvent {
    object OnBackClick : UnLockEvent()
    data class OnPinDigitChange(val digit: Int) : UnLockEvent()
    object OnDeletePinDigit : UnLockEvent()
}

//todo