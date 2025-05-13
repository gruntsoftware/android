package com.brainwallet.data.source

import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.Fee
import com.brainwallet.data.model.MoonpayCurrencyLimit
import com.brainwallet.data.source.response.GetMoonpayBuyQuoteResponse
import com.brainwallet.data.source.response.GetMoonpaySignUrlResponse
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

//TODO
interface RemoteApiSource {

    @GET("v1/rates")
    suspend fun getRates(): List<CurrencyEntity>

    @GET("v1/fee-per-kb")
    suspend fun getFeePerKb(): Fee

    @GET("v1/moonpay/ltc-to-fiat-limits")
    suspend fun getMoonpayCurrencyLimit(
        @Query("baseCurrencyCode") baseCurrencyCode: String
    ): MoonpayCurrencyLimit

    @GET("v1/moonpay/sign-url")
    suspend fun getMoonpaySignedUrl(
        @QueryMap params: Map<String, String>
    ): GetMoonpaySignUrlResponse

    @GET("v1/moonpay/buy-quote")
    suspend fun getBuyQuote(
        @QueryMap params: Map<String, String>
    ): GetMoonpayBuyQuoteResponse

}