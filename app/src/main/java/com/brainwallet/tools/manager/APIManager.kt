package com.brainwallet.tools.manager

import android.app.Activity
import android.content.Context
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.source.RemoteConfigSource
import com.brainwallet.tools.sqlite.CurrencyDataSource
import com.brainwallet.tools.util.BRConstants.BW_API_DEV_HOST
import com.brainwallet.tools.util.BRConstants.BW_API_PROD_HOST
import com.brainwallet.tools.util.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import com.squareup.moshi.Moshi
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import timber.log.Timber
import java.io.IOException
import java.util.*
import kotlin.collections.LinkedHashSet

class APIManager (private val remoteConfigSource: RemoteConfigSource) {
    fun getPRODBaseURL(): String = BW_API_PROD_HOST
    fun getDEVBaseURL(): String = BW_API_DEV_HOST

    private var timer: Timer? = null
    private var pollPeriod : Long = 4000

    private val client = OkHttpClient()
    private val moshi: Moshi = Moshi.Builder().build()
    private val type = Types.newParameterizedType(List::class.java, CurrencyEntity::class.java)
    private val jsonAdapter: JsonAdapter<List<CurrencyEntity>> = moshi.adapter(type)

    fun getCurrencies(context: Activity): Set<CurrencyEntity> {
        val set = LinkedHashSet<CurrencyEntity>()
        try {
            val arr = fetchRates(context)
            FeeManager.updateFeePerKb(context)
            arr?.let { currencyList ->
                val selectedISO = BRSharedPrefs.getIsoSymbol(context)
                currencyList.forEachIndexed { i, tempCurrencyEntity ->
                    if (tempCurrencyEntity.code.equals(selectedISO, ignoreCase = true)) {
                        BRSharedPrefs.putIso(context, tempCurrencyEntity.code)
                        BRSharedPrefs.putCurrencyListPosition(context, i - 1)
                    }
                    set.add(tempCurrencyEntity)
                }
            } ?: Timber.d("timber: getCurrencies: failed to get currencies")
        } catch (e: Exception) {
            Timber.e(e)
        }
        return set.reversed().toSet()
    }

    private fun initializeTimerTask(context: Context) {
        timer = Timer().apply {
            schedule(object : TimerTask() {
                override fun run() {
                    CoroutineScope(Dispatchers.IO).launch {
                        val tmp = getCurrencies(context as Activity)
                        withContext(Dispatchers.Main) {
                            CurrencyDataSource.getInstance(context).putCurrencies(tmp)
                        }
                    }
                }
            }, 0, pollPeriod)
        }
    }

    fun startTimer(context: Context) {
        if (timer != null) return
        initializeTimerTask(context)
    }

    fun fetchRates(activity: Activity): List<CurrencyEntity>? {
        val jsonString = createGETRequestURL(activity, BW_API_PROD_HOST + "/api/v1/rates")
        return parseJsonArray(jsonString) ?: backupFetchRates(activity)
    }

    private fun backupFetchRates(activity: Activity): List<CurrencyEntity>? {
        val jsonString = createGETRequestURL(activity, BW_API_DEV_HOST + "/api/v1/rates")
        return parseJsonArray(jsonString)
    }

    private fun parseJsonArray(jsonString: String?): List<CurrencyEntity>? {
        return try {
            jsonString?.let { jsonAdapter.fromJson(it) }
        } catch (e: IOException) {
            Timber.e(e)
            null
        }
    }

    private fun createGETRequestURL(app: Context, url: String): String? {
        val request = Request.Builder()
            .url(url)
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("User-agent", Utils.getAgentString(app, "android/HttpURLConnection"))
            .get()
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                response.body?.string().also {
                    BRSharedPrefs.putSecureTime(app, System.currentTimeMillis())
                }
            }
        } catch (e: IOException) {
            Timber.e(e)
            null
        }
    }
}
