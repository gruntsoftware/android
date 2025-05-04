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

//todo: improve this since in IDR will have big data
fun ReceiveDialogState.getAmountSequence(): Sequence<Float> {
    val (code, min, max) = moonpayCurrencyLimit.data.baseCurrency
    return generateSequence(min) { previous ->
        val next = previous + 1
        if (next > max) null else next
    }
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
