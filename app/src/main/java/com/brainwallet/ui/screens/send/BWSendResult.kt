package com.brainwallet.ui.screens.send

// SendResult.kt
sealed class BWSendResult {
    data object Success : BWSendResult()
    sealed class Error : BWSendResult() {
        data object InsufficientFunds : Error()
        data class AmountTooSmall(val minAmount: Long) : Error()
        data object AlreadySending : Error()
        data object TimedOut : Error()
        data class Unknown(val cause: Throwable) : Error()
    }
}
