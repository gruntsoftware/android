package com.brainwallet.ui.bentosections.balancebento

import android.icu.math.BigDecimal
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.presenter.entities.TxItem

data class BalanceBentoState(
    val darkMode: Boolean = true,
    val fiatCode: String = "USD",
    val symbol: String = "$",
    val topMessage: String = "Syncing...",
    val formattedTimeStamp: String = "Date: Mar 15, 2026 at 19:35",
    val syncProgress: Float = 0.5f,
    val currentBlockHeight: Int = 0,
    val lastBlock: Int = 0,
    val lastBlockLabel: String = "Last Block: %d",
    val fiatBalance: Float = 0.0f,
    val ltcBalance: BigDecimal = BigDecimal.ZERO,
    val balanceHidden: Boolean = true,
    val brainwalletIsSyncing: Boolean = true,
    val transactions: List<TxItem> = emptyList(),
    val selectedCurrency: CurrencyEntity = CurrencyEntity(
        "USD",
        "US Dollar",
        -1f,
        "$"
    )
)
