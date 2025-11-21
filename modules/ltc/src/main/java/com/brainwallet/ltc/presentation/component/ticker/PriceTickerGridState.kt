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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun rememberPriceTickerGridState(
    priceTickerStateFlow: PriceTickerStateFlow = koinInject(),
    currentCurrencyStateFlow: CurrentCurrencyStateFlow = koinInject()
): PriceTickerGridUiState {
    val priceTickerState by priceTickerStateFlow.collectAsStateWithLifecycle()
    val currentCurrency by currentCurrencyStateFlow.collectAsStateWithLifecycle()

    return remember(priceTickerState, currentCurrency) {
        PriceTickerGridUiState(
            tradingPairs = priceTickerState.tradingPairs,
            currentCurrency = currentCurrency,
            lastSyncTimestamp = priceTickerState.lastSyncTimestamp
        )
    }
}

@Stable
class PriceTickerGridUiState(
    private val tradingPairs: PersistentList<TradingPairData> = persistentListOf(),
    private val currentCurrency: String = "",
    private val lastSyncTimestamp: Long = 0
) {
    val currentTradingPair: TradingPairData? by derivedStateOf {
        tradingPairs.firstOrNull { pair ->
            pair.pairSymbol.contains(currentCurrency, ignoreCase = true)
        }
    }

    val formattedLastSyncTime: String by derivedStateOf {
        if (lastSyncTimestamp == 0L) {
            ""
        } else {
            val dateFormat = SimpleDateFormat("MMM dd 'at' hh:mma", Locale.getDefault())
            dateFormat.format(Date(lastSyncTimestamp))
        }
    }
}
