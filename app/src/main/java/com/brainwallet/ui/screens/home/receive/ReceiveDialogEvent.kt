package com.brainwallet.ui.screens.home.receive

import android.content.Context
import com.brainwallet.data.model.CurrencyEntity

sealed class ReceiveDialogEvent {
    data class OnLoad(val context: Context) : ReceiveDialogEvent()
    data class OnCopyClick(val context: Context) : ReceiveDialogEvent()
    data class OnFiatAmountChange(val amount: Float) : ReceiveDialogEvent()
    data class OnFiatCurrencyChange(val fiatCurrency: CurrencyEntity) : ReceiveDialogEvent()
}