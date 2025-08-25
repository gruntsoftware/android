package com.brainwallet.util

import android.content.Context
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.sqlite.CurrencyDataSource
import com.brainwallet.tools.util.BRCurrency
import org.koin.core.annotation.Single
import java.math.BigDecimal

@Single
class CurrencyDataGetter(
    private val context: Context,
    private val currencyDataSource: CurrencyDataSource = CurrencyDataSource.getInstance(context),
    private val isoSymbolGetter: (Context) -> String = { BRSharedPrefs.getIsoSymbol(context) },
    private val formattedCurrencyStringGetter: (Context, String, BigDecimal) -> String? =
        { context, isoCurrencyCode, amount ->
            BRCurrency.getFormattedCurrencyString(
                context,
                isoCurrencyCode,
                amount
            )
        }
) {
    fun getIsoSymbol(): String {
        return isoSymbolGetter.invoke(context)
    }

    fun getCurrencyByIso(
        iso: String
    ): CurrencyEntity? {
        return currencyDataSource.getCurrencyByIso(iso)
    }

    fun getFormattedCurrencyString(
        isoCurrencyCode: String,
        amount: BigDecimal
    ): String? {
        return formattedCurrencyStringGetter.invoke(context, isoCurrencyCode, amount)
    }
}
