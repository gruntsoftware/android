package com.brainwallet.ui.screens.home.receive

import android.graphics.Bitmap
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.MoonpayCurrencyLimit
import com.brainwallet.data.model.QuickFiatAmountOption
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


data class ReceiveDialogState(
    val address: String = "",
    val qrBitmap: Bitmap? = null,
    val fiatCurrencies: List<CurrencyEntity> = listOf(),
    val selectedFiatCurrency: CurrencyEntity = CurrencyEntity(
        "USD",
        "US Dollar",
        -1f,
        "$"
    ), // default from shared prefs then the user can override using picker wheel at [ReceiveDialog]
    val moonpayCurrencyLimit: MoonpayCurrencyLimit = MoonpayCurrencyLimit(),
    val fiatAmount: Float = 0f,
    val ltcAmount: Float = 0f,
    val ratesUpdatedAt: Long = System.currentTimeMillis(),
    val selectedQuickFiatAmountOptionIndex: Int = 1, //default is 10X, other [min, 10x, max, custom]
    val errorFiatAmountStringId: Int? = null,
    val moonpayBuySignedUrl: String? = null,
)

fun ReceiveDialogState.getSelectedFiatCurrencyIndex(): Int = fiatCurrencies
    .indexOfFirst { it.code.lowercase() == selectedFiatCurrency.code.lowercase() }

fun ReceiveDialogState.getDefaultFiatAmount(): Float {
    val (_, min, max) = moonpayCurrencyLimit.data.baseCurrency
    return min * 10 //default is 10X
}

fun ReceiveDialogState.getRatesUpdatedAtFormatted(): String {
    val date = Date(ratesUpdatedAt)
    val format = SimpleDateFormat("d MMM yyyy HH:mm:ss", Locale.ENGLISH)
    return format.format(date).uppercase()
}

fun ReceiveDialogState.getLtcAmountFormatted(isLoading: Boolean): String =
    (if (isLoading || ltcAmount < 0f) "x.xxxŁ" else "%.3fŁ".format(ltcAmount)).also {
        Timber.d("TImber:  ltcamount $ltcAmount")
    }

fun ReceiveDialogState.getQuickFiatAmountOptions(): List<QuickFiatAmountOption> {
    val selectedFiatSymbol = selectedFiatCurrency.symbol
    val (_, min, max) = moonpayCurrencyLimit.data.baseCurrency
    return listOf(min, min * 10, max)
        .map { QuickFiatAmountOption(symbol = selectedFiatSymbol, it) } + QuickFiatAmountOption()
}

fun ReceiveDialogState.isQuickFiatAmountOptionCustom(): Boolean =
    selectedQuickFiatAmountOptionIndex == 3 //3 will be custom

fun ReceiveDialogState.moonpayWidgetVisible(): Boolean = moonpayBuySignedUrl != null