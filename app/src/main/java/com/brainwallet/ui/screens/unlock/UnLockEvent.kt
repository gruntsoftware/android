package com.brainwallet.ui.screens.unlock

import android.content.Context

sealed class UnLockEvent {
    data class OnLoad(val context: Context) : UnLockEvent()
    data class OnPinDigitChange(val digit: Int) : UnLockEvent()
    object OnBackClick : UnLockEvent()
    object OnDeletePinDigit : UnLockEvent()
}

//todo