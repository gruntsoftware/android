package com.brainwallet.di

import android.content.Context
import android.content.SharedPreferences
import com.brainwallet.BuildConfig
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.data.source.RemoteConfigSource
import com.brainwallet.tools.manager.BRApiManager
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig

//TODO: revisit later
class Module(
    val context: Context,
    val remoteConfigSource: RemoteConfigSource = provideRemoteConfigSource(),
    val apiManager: BRApiManager = provideBRApiManager(remoteConfigSource),
    val sharedPreferences: SharedPreferences = provideSharedPreferences(context),
    val settingRepository: SettingRepository = provideSettingRepository(
        sharedPreferences = sharedPreferences
    )
)

private fun provideBRApiManager(remoteConfigSource: RemoteConfigSource): BRApiManager {
    return BRApiManager(remoteConfigSource)
}

private fun provideRemoteConfigSource(): RemoteConfigSource {
    return RemoteConfigSource.FirebaseImpl(Firebase.remoteConfig)
}

private fun provideSettingRepository(sharedPreferences: SharedPreferences): SettingRepository {
    return SettingRepository.Impl(sharedPreferences)
}

private fun provideSharedPreferences(
    context: Context,
    name: String = "${BuildConfig.APPLICATION_ID}.prefs"
): SharedPreferences {
    return context.getSharedPreferences(name, Context.MODE_PRIVATE)
}