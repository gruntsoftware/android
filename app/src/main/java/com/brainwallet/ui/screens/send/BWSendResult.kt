package com.brainwallet.ui.screens.send

// SendResult.kt
sealed class BWSendResult {
    /** The network relayed the transaction back — it was actually accepted. */
    data object Success : BWSendResult()
    sealed class Error : BWSendResult() {
        data object InsufficientFunds : Error()
        data class AmountTooSmall(val minAmount: Long) : Error()
        data object AlreadySending : Error()
        data object TimedOut : Error()

        /** Wallet/peer manager not ready, so nothing was handed to the network. */
        data object WalletNotReady : Error()

        /** The transaction could not be built (amount/balance). Nothing was sent. */
        data object CreationFailed : Error()

        /** Signing failed or the core refused to publish it. Nothing was sent. */
        data object SigningFailed : Error()

        /** Not connected to the Litecoin network (ENOTCONN). Nothing was sent. */
        data object NotConnected : Error()

        /** The network rejected or never relayed the transaction (EINVAL / ETIMEDOUT). */
        data class PublishRejected(val errorCode: Int) : Error()

        /**
         * The core never reported back within the app's safety timeout. The outcome is unknown,
         * so the user must check their history before retrying.
         */
        data object ConfirmationTimedOut : Error()

        data class Unknown(val cause: Throwable) : Error()
    }
}
