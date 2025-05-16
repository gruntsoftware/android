package com.brainwallet.data.model

import android.annotation.SuppressLint
import com.brainwallet.R
import com.brainwallet.tools.manager.FeeManager.ECONOMY
import com.brainwallet.tools.manager.FeeManager.FeeType
import com.brainwallet.tools.manager.FeeManager.LUXURY
import com.brainwallet.tools.manager.FeeManager.REGULAR
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Fee(
    @JvmField
    @SerialName("fee_per_kb_luxury")
    var luxury: Long,
    @JvmField
    @SerialName("fee_per_kb")
    var regular: Long,
    @JvmField
    @SerialName("fee_per_kb_economy")
    var economy: Long,
    @Transient
    var timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        private const val defaultEconomyFeePerKB: Long = 10000L
        private const val defaultRegularFeePerKB: Long = 50000L
        private const val defaultLuxuryFeePerKB: Long = 75000L

        @JvmStatic
        val Default = Fee(
            defaultLuxuryFeePerKB,
            defaultRegularFeePerKB,
            defaultEconomyFeePerKB,
        )
    }
}


data class FeeOption(
    @FeeType
    val type: String,
    val feePerKb: Long,
    val labelStringId: Int,
)

fun Fee.toFeeOptions(): List<FeeOption> = listOf(
    FeeOption(
        type = ECONOMY,
        feePerKb = economy,
        labelStringId = R.string.network_fee_options_low
    ),
    FeeOption(
        type = REGULAR,
        feePerKb = regular,
        labelStringId = R.string.network_fee_options_medium
    ),
    FeeOption(
        type = LUXURY,
        feePerKb = luxury,
        labelStringId = R.string.network_fee_options_top
    ),
)

fun FeeOption.getFiat(currencyEntity: CurrencyEntity): Float {
    val satoshisPerLtc = 100_000_000.0
    val feeInLtc = feePerKb / satoshisPerLtc
    return (feeInLtc * currencyEntity.rate).toFloat()
}

@SuppressLint("DefaultLocale")
fun FeeOption.getFiatFormatted(currencyEntity: CurrencyEntity): String {
    val fiatValue = getFiat(currencyEntity)
    val formatted = String.format("%.3f", fiatValue)
    return "${currencyEntity.symbol}$formatted"
}

fun List<FeeOption>.getSelectedIndex(selectedFeeType: String): Int {
    return indexOfFirst { it.type == selectedFeeType }.takeIf { it >= 0 }
        ?: 2  //2 -> index of top, since we have [low,medium,top]
}