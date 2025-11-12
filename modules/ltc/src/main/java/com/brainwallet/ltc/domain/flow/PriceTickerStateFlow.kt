package com.brainwallet.ltc.domain.flow

import androidx.compose.runtime.Stable
import com.brainwallet.ltc.domain.model.TradingPairData
import kotlinx.collections.immutable.PersistentList
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
@Stable
interface PriceTickerStateFlow : StateFlow<PersistentList<TradingPairData>> {
    fun refreshPrices()
}
