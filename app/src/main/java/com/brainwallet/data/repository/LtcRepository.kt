package com.brainwallet.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.net.toUri
import com.brainwallet.BuildConfig
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.Fee
import com.brainwallet.data.model.MoonpayCurrencyLimit
import com.brainwallet.data.source.RemoteApiSource
import com.brainwallet.data.source.fetchWithCache
import com.brainwallet.data.source.response.GetMoonpayBuyQuoteResponse
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.manager.FeeManager
import com.brainwallet.tools.sqlite.CurrencyDataSource
import com.brainwallet.tools.util.Utils

interface LtcRepository {
    suspend fun fetchRates(): List<CurrencyEntity>

    suspend fun fetchFeePerKb(): Fee

    suspend fun fetchLimits(baseCurrencyCode: String): MoonpayCurrencyLimit

    suspend fun fetchBuyQuote(params: Map<String, String>): GetMoonpayBuyQuoteResponse

    suspend fun fetchMoonpaySignedUrl(params: Map<String, String>): String

    class Impl(
        private val context: Context,
        private val remoteApiSource: RemoteApiSource,
        private val currencyDataSource: CurrencyDataSource,
        private val sharedPreferences: SharedPreferences,
    ) : LtcRepository {

        //todo: make it offline first here later, currently just using CurrencyDataSource.getAllCurrencies
        override suspend fun fetchRates(): List<CurrencyEntity> {
            return runCatching {
                val rates = remoteApiSource.getRates()

                //legacy logic
                FeeManager.updateFeePerKb(context)
                val selectedISO = BRSharedPrefs.getIsoSymbol(context)
                rates.forEachIndexed { index, currencyEntity ->
                    if (currencyEntity.code.equals(selectedISO, ignoreCase = true)) {
                        BRSharedPrefs.putIso(context, currencyEntity.code)
                        BRSharedPrefs.putCurrencyListPosition(context, index - 1)
                    }
                }

                //save to local
                currencyDataSource.putCurrencies(rates)
                return rates
            }.getOrElse { currencyDataSource.getAllCurrencies(true) }

        }

        /**
         * for now we just using [Fee.Default]
         * will move to [RemoteApiSource.getFeePerKb] after fix the calculation when we do send
         *
         * maybe need updaete core if we need to use dynamic fee?
         */
        override suspend fun fetchFeePerKb(): Fee = Fee.Default //using static fee

        override suspend fun fetchLimits(baseCurrencyCode: String): MoonpayCurrencyLimit {
            return sharedPreferences.fetchWithCache(
                key = "${PREF_KEY_BUY_LIMITS_PREFIX}${baseCurrencyCode.lowercase()}",
                cachedAtKey = "${PREF_KEY_BUY_LIMITS_PREFIX_CACHED_AT}${baseCurrencyCode.lowercase()}",
                cacheTimeMs = 5 * 60 * 1000, //5 minutes
                fetchData = {
                    remoteApiSource.getMoonpayCurrencyLimit(baseCurrencyCode)
                }
            )
        }

        override suspend fun fetchBuyQuote(params: Map<String, String>): GetMoonpayBuyQuoteResponse =
            remoteApiSource.getBuyQuote(params)

        override suspend fun fetchMoonpaySignedUrl(params: Map<String, String>): String {
            val externalTransactionID = Utils.getEncryptedAgentString(context)
            val finalParams = params + mapOf(
                "defaultCurrencyCode" to "ltc",
                "externalTransactionId" to externalTransactionID,
                "currencyCode" to "ltc",
                "themeId" to "main-v1.0.0",
            )
            return remoteApiSource.getMoonpaySignedUrl(finalParams)
                .signedUrl.toUri()
                .buildUpon()
                .apply {
                    if (BuildConfig.DEBUG) {
                        authority("buy-sandbox.moonpay.com")//replace base url from buy.moonpay.com
                    }
                }
                .build()
                .toString()
        }

    }

    companion object {
        const val PREF_KEY_NETWORK_FEE_PER_KB = "network_fee_per_kb"
        const val PREF_KEY_NETWORK_FEE_PER_KB_CACHED_AT = "${PREF_KEY_NETWORK_FEE_PER_KB}_cached_at"
        const val PREF_KEY_BUY_LIMITS_PREFIX = "buy_limits:" //e.g. buy_limits:usd
        const val PREF_KEY_BUY_LIMITS_PREFIX_CACHED_AT = "buy_limits_cached_at:"
    }
}