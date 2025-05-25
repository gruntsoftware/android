package com.brainwallet.data.model

import android.annotation.SuppressLint
import com.brainwallet.R
import com.brainwallet.tools.manager.FeeManager.ECONOMY
import com.brainwallet.tools.manager.FeeManager.FeeType
import com.brainwallet.tools.manager.FeeManager.LUXURY
import com.brainwallet.tools.manager.FeeManager.REGULAR
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    var timestamp: Long
) {
    companion object {
        /**
         * Default value for economy fee rate per kilobyte.
         * Used as a fallback when fee rate cannot be determined dynamically.
         *
         * Previous value: 2500L (2.5 satoshis per byte). From legacy minimum. default min is 1000 as Litecoin Core version v0.17.1
         * Updated economy to 8000L (8 satoshis per byte) on 2023-11-16 (same as iOS)
         */
        private const val defaultEconomyFeePerKB: Long = 8000L
        private const val defaultRegularFeePerKB: Long = 25000L
        private const val defaultLuxuryFeePerKB: Long = 66746L
        private const val defaultTimestamp: Long = 1583015199122L

        /**
         * currently we are using this static [Default] for our fee
         * maybe we need to update core if we need dynamic fee?
         */
        @JvmStatic
        val Default = Fee(
            defaultLuxuryFeePerKB,
            defaultRegularFeePerKB,
            defaultEconomyFeePerKB,
            defaultTimestamp
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