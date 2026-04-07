package com.brainwallet.ui.screens.main

import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.LtcStats
import com.brainwallet.data.model.MoonpayCurrencyLimit
import com.brainwallet.presenter.entities.TxItem
import com.brainwallet.ui.bentosections.transactionbento.TransactionFilterState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.math.BigDecimal

data class MainScreenState(
    val moonpayCurrencyLimit: MoonpayCurrencyLimit = MoonpayCurrencyLimit(),
    val fiatAmount: BigDecimal = BigDecimal(moonpayCurrencyLimit.data.baseCurrency.min.toDouble()),
    val ltcAmount: BigDecimal = BigDecimal(0),
    val address: String = "",
    val errorFiatAmountStringId: Int? = null,
    val fiatSymbol: String = "",
    val fiatiSOCode: String = "USD",
    val fiatRate: BigDecimal = BigDecimal(0),
    val versionLabel: String = "",
    val showTransactionDetail: Boolean = false,
    val shouldShowFiatValues: Boolean = false,
    val transactionItems: ImmutableList<TxItem> = persistentListOf(),
    val filterState: TransactionFilterState = TransactionFilterState.ALL,
    val allTransactionItems: ImmutableList<TxItem> = persistentListOf(),
    val ltcStats: LtcStats = LtcStats(
        0,
        0,
        0,
        0
    ),
    val ltcBalance: BigDecimal = BigDecimal(0),
    val litoshiBalance: BigDecimal = BigDecimal(0),
    val selectedCurrency: CurrencyEntity = CurrencyEntity(
        "USD",
        "US Dollar",
        -1f,
        "$"
    ),
    val isInternetReachable: Boolean = true,
    val formattedCurrency: String = ""
) {
    val fiatBalance: BigDecimal
        get() = litoshiBalance
            .multiply(BigDecimal(selectedCurrency.rate.toDouble()))

    val fiatBalanceFormatted: String
        get() = "%.2f".format(fiatBalance)
}
