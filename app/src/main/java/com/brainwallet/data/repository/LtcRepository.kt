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

        override suspend fun fetchFeePerKb(): Fee {
            return sharedPreferences.fetchWithCache(
                key = PREF_KEY_NETWORK_FEE_PER_KB,
                cachedAtKey = PREF_KEY_NETWORK_FEE_PER_KB_CACHED_AT,
                cacheTimeMs = 6 * 60 * 60 * 1000,
                fetchData = {
                    remoteApiSource.getFeePerKb()
                },
                defaultValue = Fee.Default
            )
        }

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
            return remoteApiSource.getMoonpaySignedUrl(params)
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