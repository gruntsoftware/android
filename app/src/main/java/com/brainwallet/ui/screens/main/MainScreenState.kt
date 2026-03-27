package com.brainwallet.ui.screens.main

import com.brainwallet.data.model.MoonpayCurrencyLimit
import com.brainwallet.presenter.entities.TxItem
import com.brainwallet.ui.bentosections.transactionbento.TransactionFilterState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import timber.log.Timber

data class MainScreenState(
    val moonpayCurrencyLimit: MoonpayCurrencyLimit = MoonpayCurrencyLimit(),
    val fiatAmount: Float = moonpayCurrencyLimit.data.baseCurrency.min,
    val ltcAmount: Float = 0f,
    val address: String = "",
    val errorFiatAmountStringId: Int? = null,
    val fiatSymbol: String = "",
    val fiatIso: String = "",
    val fiatRate: Float = 0f,
    val versionLabel: String = "",
    val showTransactionDetail: Boolean = false,
    val transactionItems: ImmutableList<TxItem> = persistentListOf(),
    val filterState: TransactionFilterState = TransactionFilterState.ALL,
)

fun MainScreenState.isValid(): Boolean = errorFiatAmountStringId == null

fun MainScreenState.getLtcAmountFormatted(isLoading: Boolean): String =
    (if (isLoading || ltcAmount < 0f) "x.xxxŁ" else "%.3fŁ".format(ltcAmount)).also {
        Timber.d("TImber:  ltcamount $ltcAmount")
    }
