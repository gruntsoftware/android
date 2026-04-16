package com.brainwallet.ui.screens.buyreceive

import android.content.Context

sealed class BuyReceiveEvent {
    data class OnLoad(val context: Context) : BuyReceiveEvent()
    data class OnFiatAmountChange(
        val fiatAmount: Float,
        val needFetch: Boolean = true
    ) : BuyReceiveEvent()
}
