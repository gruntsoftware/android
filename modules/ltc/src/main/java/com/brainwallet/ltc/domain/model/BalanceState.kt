package com.brainwallet.ltc.domain.model

data class BalanceState(
    val ltcValue: String = "",
    val valueOnCurrency: String = "",
    val balanceLitoshis: Long = 0L
)
