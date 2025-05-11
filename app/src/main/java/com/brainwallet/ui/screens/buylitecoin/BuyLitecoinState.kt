package com.brainwallet.ui.screens.buylitecoin

import com.brainwallet.data.model.MoonpayCurrencyLimit
import timber.log.Timber

data class BuyLitecoinState(
    val moonpayCurrencyLimit: MoonpayCurrencyLimit = MoonpayCurrencyLimit(),
    val fiatAmount: Float = moonpayCurrencyLimit.data.baseCurrency.min,
    val ltcAmount: Float = 0f,
    val address: String = "",
    val errorFiatAmountStringId: Int? = null
)

fun BuyLitecoinState.isValid(): Boolean = errorFiatAmountStringId == null

fun BuyLitecoinState.getLtcAmountFormatted(isLoading: Boolean): String =
    (if (isLoading || ltcAmount < 0f) "x.xxxŁ" else "%.3fŁ".format(ltcAmount)).also {
        Timber.d("TImber:  ltcamount $ltcAmount")
    }
