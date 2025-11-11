package com.brainwallet.ltc.domain.model

data class TxItem(
    val timeStamp: Long,
    val blockHeight: Int,
    val txHash: ByteArray,
    val txReversed: String,
    val sent: Long,
    val received: Long,
    val fee: Long,
    val to: List<String>,
    val from: List<String>,
    val balanceAfterTx: Long,
    val txSize: Int,
    val outAmounts: List<Long>,
    val isValid: Boolean,
    val metaData: TxMetaData? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TxItem

        if (timeStamp != other.timeStamp) return false
        if (blockHeight != other.blockHeight) return false
        if (!txHash.contentEquals(other.txHash)) return false
        if (txReversed != other.txReversed) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timeStamp.hashCode()
        result = 31 * result + blockHeight
        result = 31 * result + txHash.contentHashCode()
        result = 31 * result + txReversed.hashCode()
        return result
    }
}
