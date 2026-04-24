package com.brainwallet.wallet

interface WalletOperations {
    fun tryTransactionWithOps(sendAddress: String, sendAmount: Long, opsAddress: String, opsFeeAmount: Long): ByteArray?
    fun getMinOutputAmount(): Long
    fun getMinOutputAmountRequested(): Long
}
