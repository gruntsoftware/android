package ltd.grunt.brainwallet

import ltd.grunt.brainwallet.data.source.RemoteConfigSource
import ltd.grunt.brainwallet.di.Module
import ltd.grunt.brainwallet.util.cryptography.KeyStoreKeyGenerator
import ltd.grunt.brainwallet.util.cryptography.KeyStoreManager
import timber.log.Timber

class BrainwalletScreengrabApp : BreadApp() {

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