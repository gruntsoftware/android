package com.breadwallet

import com.litewallet.data.source.RemoteConfigSource
import com.litewallet.di.Module
import com.litewallet.util.cryptography.KeyStoreKeyGenerator
import com.litewallet.util.cryptography.KeyStoreManager

class LitewalletApp : BreadApp() {

    override fun initializeModule() {
        module = Module(
            remoteConfigSource = MockRemoteConfigSource()
        )
        module.remoteConfigSource.initialize()
        keyStoreManager = KeyStoreManager(this, KeyStoreKeyGenerator.Impl())
    }
}

class MockRemoteConfigSource : RemoteConfigSource {

    override fun initialize() {

    }

    override fun getString(key: String): String {
        return ""
    }

    override fun getNumber(key: String): Double {
        return 0.0
    }

    override fun getBoolean(key: String): Boolean {
        return true
    }

}