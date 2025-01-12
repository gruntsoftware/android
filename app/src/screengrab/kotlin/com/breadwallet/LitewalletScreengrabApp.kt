package com.breadwallet

import com.litewallet.data.source.RemoteConfigSource
import com.litewallet.di.Module
import com.litewallet.util.cryptography.KeyStoreKeyGenerator
import com.litewallet.util.cryptography.KeyStoreManager
import timber.log.Timber

class LitewalletScreengrabApp : BreadApp() {

    override fun initializeModule() {
        Timber.d("Timber: initializeModule Screengrab")
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
        Timber.d("timber: getBoolean")
        return true
    }

}