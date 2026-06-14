package com.brainwallet.ui.screens.main

import android.content.Context
import com.brainwallet.presenter.entities.TxItem
import java.math.BigDecimal

sealed class MainScreenEvent {
    data class OnLoad(val context: Context) : MainScreenEvent()
    data class OnFiatAmountChangeFromMPLimits(
        val fiatAmount: BigDecimal,
        val needFetch: Boolean = true
    ) : MainScreenEvent()
    data object OnToggleDarkMode : MainScreenEvent()
    data object OnToggleTransactionsDetail : MainScreenEvent()
    data object OnToggleTransactionsFilter : MainScreenEvent()
    data object OnExportTransactions : MainScreenEvent()
    data class OnCopyTransactions(val transactionItem: TxItem) : MainScreenEvent()

    data object OnToggleGameHub : MainScreenEvent()
}
