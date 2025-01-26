package com.brainwallet.di

import com.brainwallet.tools.manager.BRApiManager
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.brainwallet.data.source.RemoteConfigSource


class Module(
    val remoteConfigSource: com.brainwallet.data.source.RemoteConfigSource = provideRemoteConfigSource(),
    val apiManager: com.brainwallet.tools.manager.BRApiManager = provideBRApiManager(remoteConfigSource)
)

private fun provideBRApiManager(remoteConfigSource: com.litewallet.data.source.RemoteConfigSource): com.brainwallet.tools.manager.BRApiManager {
    return com.brainwallet.tools.manager.BRApiManager(remoteConfigSource)
}

private fun provideRemoteConfigSource(): com.brainwallet.data.source.RemoteConfigSource {
    return com.brainwallet.data.source.RemoteConfigSource.FirebaseImpl(Firebase.remoteConfig)
}
