package com.brainwallet.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.net.toUri
import com.brainwallet.BuildConfig
import com.brainwallet.constants.BWConstants
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.Fee
import com.brainwallet.data.model.LtcStats
import com.brainwallet.data.model.MoonpayCurrencyLimit
import com.brainwallet.data.repository.LtcRepository.Companion.PREF_KEY_BUY_LIMITS_PREFIX
import com.brainwallet.data.repository.LtcRepository.Companion.PREF_KEY_BUY_LIMITS_PREFIX_CACHED_AT
import com.brainwallet.data.source.PeerManagerSource
import com.brainwallet.data.source.RemoteApiSource
import com.brainwallet.data.source.RemoteConfigSource
import com.brainwallet.data.source.fetchWithCache
import com.brainwallet.data.source.response.GetMoonpayBuyQuoteResponse
import com.brainwallet.di.AppModule.json
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.manager.FeeManager
import com.brainwallet.tools.sqlite.CurrencyDataSource
import com.brainwallet.tools.util.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.OkHttpClient
import okhttp3.Request
import org.koin.core.annotation.Single
import timber.log.Timber

@Single(binds = [LtcRepository::class])
class LtcRepositoryImpl(
    private val context: Context,
    private val remoteApiSource: RemoteApiSource,
    private val remoteConfigSource: RemoteConfigSource,
    private val currencyDataSource: CurrencyDataSource,
    private val peerManagerSource: PeerManagerSource,
    private val repositoryScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
    private val sharedPreferences: SharedPreferences,
    private val okHttpClient: OkHttpClient,
) : LtcRepository {

//    private val _rates = MutableStateFlow<List<CurrencyEntity>>(
//        currencyDataSource.getAllCurrencies(true) // seed from cache immediately
//    )
    private val _rates = MutableSharedFlow<List<CurrencyEntity>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    ).apply {
        // seed with cached values immediately, same as before
        tryEmit(currencyDataSource.getAllCurrencies(true))
    }
    override val rates: SharedFlow<List<CurrencyEntity>> = _rates.asSharedFlow()
    private val _ltcStats = MutableStateFlow(
        LtcStats(
            0,
            0,
            0,
            0
        )
    )

    private var syncingDelay = 60_000L
    private var nonSyncingDelay = 4_000L

    @Serializable
    private data class SyncPoolerContentWrapper(
        @SerialName("sync_delay") val syncDelayValue: Long = 60_000L,
        @SerialName("non_sync_delay") val nonSyncDelayValue: Long = 4_000L
    )

    override val ltcStats: StateFlow<LtcStats> = _ltcStats.asStateFlow()

    init {

        repositoryScope.launch {
            runCatching { remoteConfigSource.fetchAndActivate() }
                .onFailure { Timber.e(it, "RemoteConfig fetch failed") }

            val rawJson = remoteConfigSource.getString(RemoteConfigSource.KEY_SYNC_POLLER)
            runCatching {
                val config = json.decodeFromString<SyncPoolerContentWrapper>(rawJson)
                syncingDelay = config.syncDelayValue
                nonSyncingDelay = config.nonSyncDelayValue
            }.onFailure { Timber.e(it, "RemoteConfig parse failed") }

            while (isActive) {
                runCatching { fetchRates() }
                    .onFailure { Timber.e(it, "fetchRates failed") }

                val delay = if (peerManagerSource.blockInfo.value.syncProgress <= 0.95f) {
                    syncingDelay
                } else {
                    nonSyncingDelay
                }
                Timber.d("timber|| duration in fetching $syncingDelay $nonSyncingDelay")
                delay(delay)
            }
        }
    }
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
            _rates.emit(result)
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
            "ipAddress" to fetchUserIpAddress(),
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

    override suspend fun fetchUserIpAddress(): String = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url(BWConstants.IPIFY_API_HOST)
                .build()
            okHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    response.body?.string()?.trim().orEmpty()
                } else {
                    ""
                }
            }
        }.onFailure {
            Timber.e(it, "fetchUserIpAddress failed")
        }.getOrDefault("")
    }
}
