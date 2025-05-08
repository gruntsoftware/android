package com.brainwallet.ui.screens.home.receive

import android.graphics.Bitmap
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.MoonpayCurrencyLimit
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
)

fun ReceiveDialogState.getSelectedFiatCurrencyIndex(): Int = fiatCurrencies
    .indexOfFirst { it.code.lowercase() == selectedFiatCurrency.code.lowercase() }

fun ReceiveDialogState.getWheelPickerAmountSize(): Int {
    val (_, min, max) = moonpayCurrencyLimit.data.baseCurrency
    return ((max.toInt() - min.toInt()) / 10) + 1
}

fun ReceiveDialogState.getWheelPickerAmountFor(index: Int = 0): Int = minOf(
    moonpayCurrencyLimit.data.baseCurrency.min.toInt() + (index * 10),
    moonpayCurrencyLimit.data.baseCurrency.max.toInt()
)

fun ReceiveDialogState.getWheelPickerFiatAmountIndex(): Int {
    val (_, min, max) = moonpayCurrencyLimit.data.baseCurrency

    if (fiatAmount < min) return 0
    if (fiatAmount > max) return getWheelPickerAmountSize() - 1
    
    return ((fiatAmount.toInt() - min.toInt()) / 10).coerceIn(0, getWheelPickerAmountSize() - 1)
}

fun ReceiveDialogState.getDefaultFiatAmount(): Float {
    val (_, min, max) = moonpayCurrencyLimit.data.baseCurrency
    val range = max - min
    return min + (range * 0.1f)
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
