package com.brainwallet.ltc.domain.model

data class TxMetaData(
    val deviceId: String? = null,
    val comment: String? = null,
    val exchangeCurrency: String? = null,
    val classVersion: Int = 0,
    val blockHeight: Int = 0,
    val exchangeRate: Double = 0.0,
    val fee: Long = 0L,
    val txSize: Int = 0,
    val creationTime: Int = 0
)
