package com.brainwallet.ltc.presentation.component.ticker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brainwallet.ltc.domain.flow.CurrentCurrencyStateFlow
import com.brainwallet.ltc.domain.flow.PriceTickerStateFlow
import com.brainwallet.ltc.domain.model.TradingPairData
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.koinInject

@Composable
fun rememberPriceTickerGridState(
    priceTickerStateFlow: PriceTickerStateFlow = koinInject(),
    currentCurrencyStateFlow: CurrentCurrencyStateFlow = koinInject()
): PriceTickerGridUiState {
    val tradingPairs by priceTickerStateFlow.collectAsStateWithLifecycle()
    val currentCurrency by currentCurrencyStateFlow.collectAsStateWithLifecycle()

    return remember(tradingPairs, currentCurrency) {
        PriceTickerGridUiState(
            tradingPairs = tradingPairs,
            currentCurrency = currentCurrency
        )
    }
}

@Stable
class PriceTickerGridUiState(
    private val tradingPairs: PersistentList<TradingPairData> = persistentListOf(),
    private val currentCurrency: String = ""
) {
    val currentTradingPair: TradingPairData? by derivedStateOf {
        tradingPairs.firstOrNull { pair ->
            pair.pairSymbol.contains(currentCurrency, ignoreCase = true)
        }
    }
}
