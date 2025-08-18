package com.brainwallet.data.repository

import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.Fee
import com.brainwallet.data.model.MoonpayCurrencyLimit
import com.brainwallet.data.source.response.GetMoonpayBuyQuoteResponse

interface LtcRepository {
    suspend fun fetchRates(): List<CurrencyEntity>

    suspend fun fetchFeePerKb(): Fee

    suspend fun fetchLimits(baseCurrencyCode: String): MoonpayCurrencyLimit

    suspend fun fetchBuyQuote(params: Map<String, String>): GetMoonpayBuyQuoteResponse

    suspend fun fetchMoonpaySignedUrl(params: Map<String, String>): String

    companion object {
        const val PREF_KEY_NETWORK_FEE_PER_KB = "network_fee_per_kb"
        const val PREF_KEY_NETWORK_FEE_PER_KB_CACHED_AT = "${PREF_KEY_NETWORK_FEE_PER_KB}_cached_at"
        const val PREF_KEY_BUY_LIMITS_PREFIX = "buy_limits:" //e.g. buy_limits:usd
        const val PREF_KEY_BUY_LIMITS_PREFIX_CACHED_AT = "buy_limits_cached_at:"
    }
}