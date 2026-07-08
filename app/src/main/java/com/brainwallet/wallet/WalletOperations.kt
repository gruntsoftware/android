package com.brainwallet.wallet

import kotlinx.collections.immutable.ImmutableList

interface WalletOperations {
    fun tryTransactionWithOps(sendAddress: String, sendAmount: Long, opsAddress: String, opsFeeAmount: Long): ByteArray?
    fun getMinOutputAmount(): Long
    fun getMinOutputAmountRequested(): Long

    fun getSeedWords(): ImmutableList<String>
}
