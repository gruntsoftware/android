package com.brainwallet.domain.mapper

import com.platform.entities.TxMetaData as JavaTxMetaData
import com.brainwallet.ltc.domain.model.TxMetaData as KotlinTxMetaData

fun JavaTxMetaData.toKotlin(): KotlinTxMetaData {
    return KotlinTxMetaData(
        deviceId = deviceId,
        comment = comment,
        exchangeCurrency = exchangeCurrency,
        classVersion = classVersion,
        blockHeight = blockHeight,
        exchangeRate = exchangeRate,
        fee = fee,
        txSize = txSize,
        creationTime = creationTime
    )
}

fun KotlinTxMetaData.toJava(): JavaTxMetaData {
    return JavaTxMetaData().apply {
        deviceId = this@toJava.deviceId
        comment = this@toJava.comment
        exchangeCurrency = this@toJava.exchangeCurrency
        classVersion = this@toJava.classVersion
        blockHeight = this@toJava.blockHeight
        exchangeRate = this@toJava.exchangeRate
        fee = this@toJava.fee
        txSize = this@toJava.txSize
        creationTime = this@toJava.creationTime
    }
}
