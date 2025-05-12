package com.brainwallet.data.source.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetMoonpayBuyQuoteResponse(
    @SerialName("data") val data: Data = Data()

) {
    @Serializable
    data class Data(
        val totalAmount: Float = 0f,
        val baseCurrencyCode: String = "usd",
        val quoteCurrencyAmount: Float = 0f,
    )
}
