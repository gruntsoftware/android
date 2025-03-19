package com.brainwallet.ui.screens.unlock

import android.content.Context

sealed class UnLockEvent {
    data class OnLoad(
        val context: Context,
        val isUpdatePin: Boolean = false,
    ) : UnLockEvent()
    data class OnPinDigitChange(
        val digit: Int,
        val isValidPin: (String) -> Boolean
    ) : UnLockEvent()
    object OnDeletePinDigit : UnLockEvent()
}

//todo