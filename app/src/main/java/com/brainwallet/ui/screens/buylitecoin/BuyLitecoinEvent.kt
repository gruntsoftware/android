package com.brainwallet.ui.screens.buylitecoin

import android.content.Context

sealed class BuyLitecoinEvent {
    data class OnLoad(val context: Context) : BuyLitecoinEvent()
    data class OnFiatAmountChange(val fiatAmount: Float) : BuyLitecoinEvent()
}