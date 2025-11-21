package com.brainwallet.ltc.domain.model

import kotlinx.collections.immutable.PersistentList

data class PriceTickerState(
    val tradingPairs: PersistentList<TradingPairData>,
    val lastSyncTimestamp: Long
)
