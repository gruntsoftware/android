package com.brainwallet.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Fee(
    @JvmField
    @SerialName("fee_per_kb")
    var luxury: Long,
    @JvmField
    @SerialName("fee_per_kb_economy")
    var regular: Long,
    @JvmField
    @SerialName("fee_per_kb_luxury")
    var economy: Long,
    var timestamp: Long
) {
    companion object {
        //from legacy
        // this is the default that matches the mobile-api if the server is unavailable
        private const val defaultEconomyFeePerKB: Long =
            2500L // From legacy minimum. default min is 1000 as Litecoin Core version v0.17.1
        private const val defaultRegularFeePerKB: Long = 25000L
        private const val defaultLuxuryFeePerKB: Long = 66746L
        private const val defaultTimestamp: Long = 1583015199122L

        @JvmStatic
        val Default = Fee(
            defaultLuxuryFeePerKB,
            defaultRegularFeePerKB,
            defaultEconomyFeePerKB,
            defaultTimestamp
        )

    }
}
