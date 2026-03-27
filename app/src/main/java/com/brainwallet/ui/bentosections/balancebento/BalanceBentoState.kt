package com.brainwallet.ui.bentosections.balancebento

import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.presenter.entities.TxItem
import java.math.BigDecimal
import kotlin.text.toDouble
import kotlin.text.toFloat

data class BalanceBentoState(
    val darkMode: Boolean = true,
    val fiatCode: String = "USD",
    val symbol: String = "$",
    val topMessage: String = "Syncing...",
    val lastTimeStamp: String = "",
    val syncProgress: Float = 0.5f,
    val currentBlockHeight: Int = 0,
    val lastBlock: Int = 0,
    val ltcBalance: Long = 0L,
    val litoshiBalance: BigDecimal = BigDecimal(0),
    val balanceHidden: Boolean = true,
    val brainwalletIsSyncing: Boolean = true,
    val transactions: List<TxItem> = emptyList(),
    val selectedCurrency: CurrencyEntity = CurrencyEntity(
        "USD",
        "US Dollar",
        -1f,
        "$"
    ),
    val isInternetReachable: Boolean = true
) {
    val fiatBalance: Float
        get() = litoshiBalance
            .multiply(BigDecimal(selectedCurrency.rate.toDouble()))
            .toFloat()
    val fiatBalanceFormatted: String
        get() = "%.2f".format(fiatBalance)
}
