package com.brainwallet.di

import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.tools.manager.BRApiManager
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.brainwallet.data.source.RemoteConfigSource


class Module(
    val remoteConfigSource: RemoteConfigSource = provideRemoteConfigSource(),
    val apiManager: BRApiManager = provideBRApiManager(remoteConfigSource),
    val settingRepository: SettingRepository = provideSettingRepository()
)

private fun provideBRApiManager(remoteConfigSource: RemoteConfigSource): BRApiManager {
    return BRApiManager(remoteConfigSource)
}

private fun provideRemoteConfigSource(): RemoteConfigSource {
    return RemoteConfigSource.FirebaseImpl(Firebase.remoteConfig)
}

private fun provideSettingRepository(): SettingRepository {
    return SettingRepository.Impl()
}