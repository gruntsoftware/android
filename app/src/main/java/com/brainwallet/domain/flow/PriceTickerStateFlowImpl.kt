package com.brainwallet.domain.flow

import android.content.Context
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.repository.LtcRepository
import com.brainwallet.ltc.domain.flow.PriceTickerStateFlow
import com.brainwallet.ltc.domain.model.PriceTickerState
import com.brainwallet.ltc.domain.model.TradingPairData
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.util.BRCurrency
import com.brainwallet.tools.util.BRExchange
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import java.math.BigDecimal
import kotlin.collections.map

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
@Single
class PriceTickerStateFlowImpl(
    private val context: Context,
    private val ltcRepository: LtcRepository,
    private val upstream: MutableStateFlow<PriceTickerState> = MutableStateFlow(
        PriceTickerState(
            tradingPairs = defaultTradingPairs,
            lastSyncTimestamp = 0L
        )
    )
) : PriceTickerStateFlow, StateFlow<PriceTickerState> by upstream {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        refreshPrices()
    }

    override fun refreshPrices() {
        scope.launch {
            val rates = ltcRepository.fetchRates()
            val tradingPairs = mapRatesToTradingPairs(rates)
            val lastSyncTimestamp = BRSharedPrefs.getLastPriceSyncTimestamp(context)

            if (tradingPairs.isNotEmpty()) {
                upstream.update {
                    PriceTickerState(
                        tradingPairs = tradingPairs,
                        lastSyncTimestamp = lastSyncTimestamp
                    )
                }
            }
        }
    }

    private fun mapRatesToTradingPairs(rates: List<CurrencyEntity>): PersistentList<TradingPairData> {
        val oneLitecoinInLitoshis = BigDecimal(BRExchange.ONE_LITECOIN_OF_LITOSHIS)

        // We use the rates returned from the repo to drive the list,
        // ensuring we show ALL supported currencies, not just a filtered list.
        val tradingPairs = rates.map { currencyEntity ->
            val isoCode = currencyEntity.code

            // logic preserved from original code using BRExchange/BRCurrency
            // to ensure price calculations remain identical.
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

        return tradingPairs.toPersistentList()
    }

    private fun formatPrice(price: Double): String {
        return when {
            price >= 1000 -> String.format("%.2f", price).replace(",", " ")
            price >= 100 -> String.format("%.2f", price)
            price >= 10 -> String.format("%.3f", price)
            else -> String.format("%.4f", price)
        }
    }
}

// Kept as fallback for the initial StateFlow value
private val defaultTradingPairs = persistentListOf(
    TradingPairData("LTC/USD", 115.96, "$115.96"),
    TradingPairData("LTC/EUR", 108.45, "€108.45"),
    TradingPairData("LTC/GBP", 92.18, "£92.18"),
    TradingPairData("LTC/JPY", 17850.0, "¥17,850")
)
