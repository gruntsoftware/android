package com.brainwallet.ui.screens.home

import android.content.Context

sealed class MainScreenEvent {
    data class OnLoad(val context: Context) : MainScreenEvent()
    data class OnFiatAmountChange(
        val fiatAmount: Float,
        val needFetch: Boolean = true
    ) : MainScreenEvent()
}
