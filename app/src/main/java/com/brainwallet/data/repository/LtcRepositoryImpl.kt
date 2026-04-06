package com.brainwallet.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.net.toUri
import com.brainwallet.BuildConfig
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.Fee
import com.brainwallet.data.model.LtcStats
import com.brainwallet.data.model.MoonpayCurrencyLimit
import com.brainwallet.data.repository.LtcRepository.Companion.PREF_KEY_BUY_LIMITS_PREFIX
import com.brainwallet.data.repository.LtcRepository.Companion.PREF_KEY_BUY_LIMITS_PREFIX_CACHED_AT
import com.brainwallet.data.source.RemoteApiSource
import com.brainwallet.data.source.fetchWithCache
import com.brainwallet.data.source.response.GetMoonpayBuyQuoteResponse
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.manager.FeeManager
import com.brainwallet.tools.sqlite.CurrencyDataSource
import com.brainwallet.tools.util.Utils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Single

@Single(binds = [LtcRepository::class])
class LtcRepositoryImpl(
    private val context: Context,
    private val remoteApiSource: RemoteApiSource,
    private val currencyDataSource: CurrencyDataSource,
    private val sharedPreferences: SharedPreferences,
) : LtcRepository {

    private val _rates = MutableStateFlow<List<CurrencyEntity>>(emptyList())
    override val rates: StateFlow<List<CurrencyEntity>> = _rates.asStateFlow()
    private val _ltcStats = MutableStateFlow(
        LtcStats(
            0,
            0,
            0,
            0
        )
    )
    override val ltcStats: StateFlow<LtcStats> = _ltcStats.asStateFlow()

    override suspend fun fetchRates(): List<CurrencyEntity> {
        return runCatching {
            val rates = remoteApiSource.getRates()

            FeeManager.updateFeePerKb(context)
            val selectedISO = BRSharedPrefs.getIsoSymbol(context)
            rates.forEachIndexed { index, currencyEntity ->
                if (currencyEntity.code.equals(selectedISO, ignoreCase = true)) {
                    BRSharedPrefs.putIso(context, currencyEntity.code)
                    BRSharedPrefs.putCurrencyListPosition(context, index - 1)
                }
            }

            val liveLtcStats = remoteApiSource.getLtcStats()
            _ltcStats.value = liveLtcStats
            currencyDataSource.putCurrencies(rates)
            rates
        }.getOrElse {
            currencyDataSource.getAllCurrencies(true)
        }.also { result ->
            _rates.value = result
        }
    }

    override suspend fun fetchFeePerKb(): Fee {
        return runCatching {
            val feesPerKb = remoteApiSource.getFeePerKb()
            Fee(
                feesPerKb.economy,
                feesPerKb.regular,
                feesPerKb.luxury,
                System.currentTimeMillis()
            )
        }.getOrElse {
            Fee.Default
        }
    }

    override suspend fun fetchLtcStats(): LtcStats {
        return remoteApiSource.getLtcStats()
            .also { _ltcStats.value = it }
    }

    override suspend fun fetchLimits(baseCurrencyCode: String): MoonpayCurrencyLimit {
        return sharedPreferences.fetchWithCache(
            key = "${PREF_KEY_BUY_LIMITS_PREFIX}${baseCurrencyCode.lowercase()}",
            cachedAtKey = "${PREF_KEY_BUY_LIMITS_PREFIX_CACHED_AT}${baseCurrencyCode.lowercase()}",
            cacheTimeMs = 5 * 60 * 1000, // 5 minutes
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
                    authority("buy-sandbox.moonpay.com") // replace base url from buy.moonpay.com
                }
            }
            .build()
            .toString()
    }
}
