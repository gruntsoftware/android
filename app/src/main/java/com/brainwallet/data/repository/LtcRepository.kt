package com.brainwallet.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.Fee
import com.brainwallet.data.source.RemoteApiSource
import com.brainwallet.di.json
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.manager.FeeManager
import com.brainwallet.tools.sqlite.CurrencyDataSource
import kotlinx.serialization.encodeToString

interface LtcRepository {
    suspend fun fetchRates(): List<CurrencyEntity>

    suspend fun fetchFeePerKb(): Fee

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
            val lastUpdateTime = sharedPreferences.getLong(PREF_KEY_NETWORK_FEE_PER_KB_CACHED_AT, 0)
            val currentTime = System.currentTimeMillis()
            val cachedFee = sharedPreferences.getString(PREF_KEY_NETWORK_FEE_PER_KB, null)
                ?.let { json.decodeFromString<Fee>(it) }

            return runCatching {
                // Check if cache exists and is less than 6 hours old
                if (cachedFee != null && (currentTime - lastUpdateTime) < 6 * 60 * 60 * 1000) {
                    return cachedFee
                }

                val fee = remoteApiSource.getFeePerKb()
                sharedPreferences.edit {
                    putString(PREF_KEY_NETWORK_FEE_PER_KB, json.encodeToString(fee))
                    putLong(PREF_KEY_NETWORK_FEE_PER_KB_CACHED_AT, currentTime)
                }

                return fee
            }.getOrElse {
                cachedFee ?: Fee.Default
            }
        }

    }

    companion object {
        const val PREF_KEY_NETWORK_FEE_PER_KB = "network_fee_per_kb"
        const val PREF_KEY_NETWORK_FEE_PER_KB_CACHED_AT = "${PREF_KEY_NETWORK_FEE_PER_KB}_cached_at"
    }
}