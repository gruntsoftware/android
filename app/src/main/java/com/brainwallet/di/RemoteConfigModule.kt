package com.brainwallet.di

import com.brainwallet.data.repository.FirebaseRemoteConfigRepositoryImpl
import com.brainwallet.data.repository.HubContentRepository
import com.brainwallet.data.repository.HubContentRepositoryImpl
import com.brainwallet.data.source.RemoteConfigSource
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import org.koin.dsl.module

val remoteConfigModule = module {
    single {
        FirebaseRemoteConfig.getInstance().apply {
            setDefaultsAsync(
                mapOf(RemoteConfigSource.KEY_FEATURE_GAMEHUB_CONTENT to "[]")
            )
        }
    }
    single<RemoteConfigSource> { FirebaseRemoteConfigRepositoryImpl(get()) }
    single<HubContentRepository> { HubContentRepositoryImpl(get(), get()) }
}
