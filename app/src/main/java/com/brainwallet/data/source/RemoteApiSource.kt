package com.brainwallet.data.source

import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.Fee
import com.brainwallet.data.model.MoonpayCurrencyLimit
import retrofit2.http.GET
import retrofit2.http.Query

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

//    https://prod.apigsltd.net/moonpay/buy?address=ltc1qjnsg3p9rt4r4vy7ncgvrywdykl0zwhkhcp8ue0&code=USD&idate=1742331930290&uid=ec51fa950b271ff3
//    suspend fun getMoonPayBuy()

//    v1/moonpay/network-fees?fiatCurrencies=usd
//    {
//        "data": {
//        "LTC": {
//        "USD": 0.29
//    }
//    }
//    }

//    v1/moonpay/ltc-to-fiat-limits?baseCurrencyCode=usd
//    {
//        "data": {
//        "paymentMethod": "moonpay_balance",
//        "quoteCurrency": {
//        "code": "ltc",
//        "minBuyAmount": 0.218,
//        "maxBuyAmount": 327.427
//    },
//        "baseCurrency": {
//        "code": "usd",
//        "minBuyAmount": 21,
//        "maxBuyAmount": 29849
//    },
//        "areFeesIncluded": false
//    }
//    }
}