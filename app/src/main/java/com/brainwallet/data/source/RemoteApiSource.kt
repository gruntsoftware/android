package com.brainwallet.data.source

import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.Fee
import retrofit2.http.GET

//TODO
interface RemoteApiSource {

    @GET("v1/rates")
    suspend fun getRates(): List<CurrencyEntity>

    @GET("v1/fee-per-kb")
    suspend fun getFeePerKb(): Fee

//    https://prod.apigsltd.net/moonpay/buy?address=ltc1qjnsg3p9rt4r4vy7ncgvrywdykl0zwhkhcp8ue0&code=USD&idate=1742331930290&uid=ec51fa950b271ff3
//    suspend fun getMoonPayBuy()
}