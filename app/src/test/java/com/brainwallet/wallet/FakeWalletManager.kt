package com.brainwallet.wallet

class FakeWalletManager(
    private val created: Boolean = true,
    private val addressValid: Boolean = true
) : WalletManager {
    override fun isCreated() = created
    override fun validateAddress(address: String) = addressValid
}
