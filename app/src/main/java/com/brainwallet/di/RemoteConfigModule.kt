package com.brainwallet.di

import com.brainwallet.data.repository.FirebaseRemoteConfigRepositoryImpl
import com.brainwallet.data.repository.HubContentRepository
import com.brainwallet.data.repository.HubContentRepositoryImpl
import com.brainwallet.data.repository.ShopProxyRepository
import com.brainwallet.data.repository.ShopProxyRepositoryImpl
import com.brainwallet.data.source.RemoteConfigSource
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import org.koin.dsl.module

val remoteConfigModule = module {
    single {
        FirebaseRemoteConfig.getInstance().apply {
            setDefaultsAsync(
                mapOf(
                    RemoteConfigSource.KEY_FEATURE_GAMEHUB_CONTENT to "[]",
                    RemoteConfigSource.PATH_SHOP_CONTENT to "{\"shop_data\":{}}"
                )
            )
        }
    }
    single<RemoteConfigSource> { FirebaseRemoteConfigRepositoryImpl(get()) }
    single<HubContentRepository> { HubContentRepositoryImpl(get(), get()) }
    single<ShopProxyRepository> {
        ShopProxyRepositoryImpl(
            remoteConfigSource = get(),
            json = get(),
            scope = get(),
        )
    }
}
