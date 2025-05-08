package com.brainwallet.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MoonpayCurrencyLimit(
    @SerialName("data") val data: Data = Data()
) {
    @Serializable
    data class Data(
        @SerialName("paymentMethod") val paymentMethod: String = "",
        @SerialName("quoteCurrency") val quoteCurrency: CurrencyLimit = CurrencyLimit(),
        @SerialName("baseCurrency") val baseCurrency: CurrencyLimit = CurrencyLimit(),
        @SerialName("areFeesIncluded") val areFeesIncluded: Boolean = false
    )

    @Serializable
    data class CurrencyLimit(
        val code: String = "usd",
        @SerialName("minBuyAmount") val min: Float = 21f,
        @SerialName("maxBuyAmount") val max: Float = 29849f
    )
}