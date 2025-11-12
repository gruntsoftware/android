package com.brainwallet.ltc.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brainwallet.ltc.domain.flow.PriceTickerStateFlow
import com.brainwallet.ltc.domain.model.TradingPairData
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import org.koin.compose.koinInject

@Composable
fun rememberPriceTickerGridState(
    priceTickerStateFlow: PriceTickerStateFlow = koinInject()
): PriceTickerGridUiState {
    val tradingPairs by priceTickerStateFlow.collectAsStateWithLifecycle()
    val state = remember {
        PriceTickerGridUiState(
            initialTradingPairs = tradingPairs
        )
    }
    LaunchedEffect(tradingPairs) {
        state.updateTradingPairs(tradingPairs)
    }
    return state
}

@Stable
class PriceTickerGridUiState(
    initialTradingPairs: PersistentList<TradingPairData> = persistentListOf()
) {
    var tradingPairs by mutableStateOf(initialTradingPairs)
        private set

    fun updateTradingPairs(tradingPairs: PersistentList<TradingPairData>) {
        this.tradingPairs = tradingPairs
    }
}
