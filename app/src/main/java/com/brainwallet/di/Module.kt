package com.brainwallet.di

import android.content.Context
import android.content.SharedPreferences
import com.brainwallet.BuildConfig
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.data.source.RemoteConfigSource
import com.brainwallet.tools.manager.APIManager
import com.brainwallet.tools.sqlite.CurrencyDataSource
import com.brainwallet.ui.screens.home.SettingsViewModel
import com.brainwallet.ui.screens.inputwords.InputWordsViewModel
import com.brainwallet.ui.screens.ready.ReadyViewModel
import com.brainwallet.ui.screens.setpasscode.SetPasscodeViewModel
import com.brainwallet.ui.screens.unlock.UnLockViewModel
import com.brainwallet.ui.screens.welcome.WelcomeViewModel
import com.brainwallet.ui.screens.yourseedproveit.YourSeedProveItViewModel
import com.brainwallet.ui.screens.yourseedwords.YourSeedWordsViewModel
import com.brainwallet.util.cryptography.KeyStoreKeyGenerator
import com.brainwallet.util.cryptography.KeyStoreManager
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

//todo module using koin as di framework here

val dataModule = module {
    single<RemoteConfigSource> {
        RemoteConfigSource.FirebaseImpl(Firebase.remoteConfig).also {
            it.initialize()
        }
    }
    single { APIManager(get()) }
    single { CurrencyDataSource.getInstance(get()) }
    single<SharedPreferences> { provideSharedPreferences(context = androidApplication()) }
    single<SettingRepository> { SettingRepository.Impl(get(), get()) }
}

val viewModelModule = module {
    viewModelOf(::WelcomeViewModel)
    viewModelOf(::SettingsViewModel)
    viewModel { ReadyViewModel() }
    viewModel { InputWordsViewModel() }
    viewModel { SetPasscodeViewModel() }
    viewModel { UnLockViewModel() }
    viewModel { YourSeedProveItViewModel() }
    viewModel { YourSeedWordsViewModel() }
}

val appModule = module {
    single<KeyStoreManager> { KeyStoreManager(get(), KeyStoreKeyGenerator.Impl()) }
}

private fun provideSharedPreferences(
    context: Context,
    name: String = "${BuildConfig.APPLICATION_ID}.prefs"
): SharedPreferences {
    return context.getSharedPreferences(name, Context.MODE_PRIVATE)
}