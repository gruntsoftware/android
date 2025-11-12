package com.brainwallet.domain.flow

import android.content.Context
import com.brainwallet.ltc.domain.flow.PriceTickerStateFlow
import com.brainwallet.ltc.domain.model.TradingPairData
import com.brainwallet.tools.sqlite.CurrencyDataSource
import com.brainwallet.tools.util.BRCurrency
import com.brainwallet.tools.util.BRExchange
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Single
import java.math.BigDecimal

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
@Single
class PriceTickerStateFlowImpl(
    private val context: Context,
    private val upstream: MutableStateFlow<PersistentList<TradingPairData>> = MutableStateFlow(
        getTradingPairsFromExchangeRates(context)
    )
) : PriceTickerStateFlow, StateFlow<PersistentList<TradingPairData>> by upstream {

    override fun refreshPrices() {
        upstream.value = getTradingPairsFromExchangeRates(context)
    }
}

private fun getTradingPairsFromExchangeRates(context: Context): PersistentList<TradingPairData> {
    val currencyDataSource = CurrencyDataSource.getInstance(context)
    val oneLitecoinInLitoshis = BigDecimal(BRExchange.ONE_LITECOIN_OF_LITOSHIS)

    val supportedCurrencies = listOf("USD", "EUR", "GBP", "JPY")

    val tradingPairs = supportedCurrencies.mapNotNull { isoCode ->
        val currencyEntity = currencyDataSource.getCurrencyByIso(isoCode)
        currencyEntity?.let {
            val priceInCurrency = BRExchange.getAmountFromLitoshis(
                context,
                isoCode,
                oneLitecoinInLitoshis
            )
            val formattedPrice = BRCurrency.getFormattedCurrencyString(
                context,
                isoCode,
                priceInCurrency
            ) ?: formatPrice(priceInCurrency.toDouble())

            TradingPairData(
                pairSymbol = "LTC/$isoCode",
                price = priceInCurrency.toDouble(),
                formattedPrice = formattedPrice
            )
        }
    }

    return if (tradingPairs.isNotEmpty()) {
        tradingPairs.toPersistentList()
    } else {
        defaultTradingPairs
    }
}

private fun formatPrice(price: Double): String {
    return when {
        price >= 1000 -> String.format("%.2f", price).replace(",", " ")
        price >= 100 -> String.format("%.2f", price)
        price >= 10 -> String.format("%.3f", price)
        else -> String.format("%.4f", price)
    }
}

private val defaultTradingPairs = persistentListOf(
    TradingPairData("LTC/USD", 115.96, "$115.96"),
    TradingPairData("LTC/EUR", 108.45, "€108.45"),
    TradingPairData("LTC/GBP", 92.18, "£92.18"),
    TradingPairData("LTC/JPY", 17850.0, "¥17,850")
)
