package com.brainwallet.ltc.domain.model

data class TradingPairData(
    val pairSymbol: String,
    val price: Double,
    val formattedPrice: String
)
