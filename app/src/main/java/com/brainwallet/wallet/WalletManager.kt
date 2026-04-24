package com.brainwallet.wallet

interface WalletManager {
    fun isCreated(): Boolean
    fun validateAddress(address: String): Boolean
}
