package com.brainwallet.domain.mapper

import com.brainwallet.ltc.domain.model.TxItem as KotlinTxItem
import com.brainwallet.presenter.entities.TxItem as JavaTxItem

fun JavaTxItem.toKotlin(): KotlinTxItem {
    return KotlinTxItem(
        timeStamp = timeStamp,
        blockHeight = blockHeight,
        txHash = txHash,
        txReversed = txReversed,
        sent = sent,
        received = received,
        fee = fee,
        to = to?.toList() ?: emptyList(),
        from = from?.toList() ?: emptyList(),
        balanceAfterTx = balanceAfterTx,
        txSize = txSize,
        outAmounts = outAmounts?.toList() ?: emptyList(),
        isValid = isValid,
        metaData = metaData?.toKotlin()
    )
}

fun List<JavaTxItem>.toKotlin(): List<KotlinTxItem> {
    return map { it.toKotlin() }
}
