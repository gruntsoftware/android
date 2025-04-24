package com.brainwallet.data.repository

import android.content.Context
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.source.RemoteApiSource
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.manager.FeeManager
import com.brainwallet.tools.sqlite.CurrencyDataSource

interface LtcRepository {
    suspend fun fetchRates(): List<CurrencyEntity>
    //todo

    class Impl(
        private val context: Context,
        private val remoteApiSource: RemoteApiSource,
        private val currencyDataSource: CurrencyDataSource
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

    }
}