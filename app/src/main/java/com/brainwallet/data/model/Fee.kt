package com.brainwallet.data.model

import android.annotation.SuppressLint
import com.brainwallet.R
import com.brainwallet.tools.manager.FeeManager.ECONOMY
import com.brainwallet.tools.manager.FeeManager.FeeType
import com.brainwallet.tools.manager.FeeManager.LUXURY
import com.brainwallet.tools.manager.FeeManager.REGULAR
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.ceil
import kotlin.math.round

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
) {
    companion object {
        //from legacy
        // this is the default that matches the mobile-api if the server is unavailable
        private const val defaultEconomyFeePerKB: Long =
            2500L // From legacy minimum. default min is 1000 as Litecoin Core version v0.17.1
        private const val defaultRegularFeePerKB: Long = 25000L
        private const val defaultLuxuryFeePerKB: Long = 66746L
        private const val defaultTimestamp: Long = 1583015199122L

//        {"fee_per_kb":5289,"fee_per_kb_economy":2645,"fee_per_kb_luxury":10578}

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