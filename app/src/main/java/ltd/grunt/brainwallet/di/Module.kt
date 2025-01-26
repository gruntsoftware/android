package ltd.grunt.brainwallet.di

import ltd.grunt.brainwallet.tools.manager.BRApiManager
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import ltd.grunt.brainwallet.data.source.RemoteConfigSource

class Module(
    val remoteConfigSource: RemoteConfigSource = provideRemoteConfigSource(),
    val apiManager: BRApiManager = provideBRApiManager(remoteConfigSource)
)

private fun provideBRApiManager(remoteConfigSource: RemoteConfigSource): BRApiManager {
    return BRApiManager(remoteConfigSource)
}

private fun provideRemoteConfigSource(): RemoteConfigSource {
    return RemoteConfigSource.FirebaseImpl(Firebase.remoteConfig)
}
