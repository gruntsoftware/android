package com.brainwallet.ui.screens.home.receive

import android.content.Context
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.QuickFiatAmountOption

sealed class ReceiveDialogEvent {
    data class OnLoad(val context: Context) : ReceiveDialogEvent()
    data class OnCopyClick(val context: Context) : ReceiveDialogEvent()
    data class OnFiatAmountChange(val fiatAmount: Float, val needFetch: Boolean = true) : ReceiveDialogEvent()
    data class OnFiatCurrencyChange(val fiatCurrency: CurrencyEntity) : ReceiveDialogEvent()
    data class OnFiatAmountOptionIndexChange(
        val index: Int,
        val quickFiatAmountOption: QuickFiatAmountOption
    ) : ReceiveDialogEvent()
}