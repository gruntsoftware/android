package com.brainwallet.util.cryptography

import javax.crypto.SecretKey

interface KeyStoreKeyGenerator {
    fun generateKey(alias: String, isAuthRequired: Boolean, authTimeout: Int?): SecretKey
}