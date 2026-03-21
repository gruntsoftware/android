package com.brainwallet.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LtcStats(
    @JvmField
    @SerialName("blocks")
    var currentBlockHeight: Int,
    @JvmField
    @SerialName("mempool_transactions")
    var mempoolTransactions: Int,
    @JvmField
    @SerialName("mempool_size")
    var mempoolSize: Int,
    @JvmField
    @SerialName("transactions_24h")
    var transactionsOver24H: Int
) {
    companion object {
    }
}
