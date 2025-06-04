package com.brainwallet

import com.brainwallet.data.source.RemoteConfigSource
import com.brainwallet.di.appModule
import com.brainwallet.di.dataModule
import com.brainwallet.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level
import timber.log.Timber

class BrainwalletScreengrabApp : BrainwalletApp() {

    override fun initializeModule() {
        Timber.d("Timber: initializeModule Screengrab")
//        module = Module(
//            remoteConfigSource = MockRemoteConfigSource()
//        )
//        module.remoteConfigSource.initialize()
//        keyStoreManager = KeyStoreManager(this, KeyStoreKeyGenerator.Impl())

        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR)
            androidContext(this@BrainwalletScreengrabApp)
            modules(dataModule, viewModelModule, appModule)
        }
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