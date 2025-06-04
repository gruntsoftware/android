package com.brainwallet.di

import android.content.Context
import android.content.SharedPreferences
import com.brainwallet.BuildConfig
import com.brainwallet.data.repository.LtcRepository
import com.brainwallet.data.repository.SelectedPeersRepository
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.data.source.RemoteApiSource
import com.brainwallet.data.source.RemoteConfigSource
import com.brainwallet.tools.sqlite.CurrencyDataSource
import com.brainwallet.tools.util.BRConstants
import com.brainwallet.ui.screens.buylitecoin.BuyLitecoinViewModel
import com.brainwallet.ui.screens.home.SettingsViewModel
import com.brainwallet.ui.screens.home.receive.ReceiveDialogViewModel
import com.brainwallet.ui.screens.inputwords.InputWordsViewModel
import com.brainwallet.ui.screens.ready.ReadyViewModel
import com.brainwallet.ui.screens.setpasscode.SetPasscodeViewModel
import com.brainwallet.ui.screens.unlock.UnLockViewModel
import com.brainwallet.ui.screens.welcome.WelcomeViewModel
import com.brainwallet.ui.screens.yourseedproveit.YourSeedProveItViewModel
import com.brainwallet.ui.screens.yourseedwords.YourSeedWordsViewModel
import com.brainwallet.util.cryptography.KeyStoreKeyGenerator
import com.brainwallet.util.cryptography.KeyStoreManager
import com.brainwallet.worker.CurrencyUpdateWorker
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

//todo module using koin as di framework here

val json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    prettyPrint = true
}

val dataModule = module {
    factory { provideOkHttpClient() }
    single { provideRetrofit(get(), BRConstants.BW_API_PROD_HOST) }

    single { provideApi<RemoteApiSource>(get()) }

    single<RemoteConfigSource> {
        RemoteConfigSource.FirebaseImpl(Firebase.remoteConfig).also {
            it.initialize()
        }
    }
    single<SelectedPeersRepository> { SelectedPeersRepository.Impl(get(), get()) }
    single { CurrencyDataSource.getInstance(get()) }
    single<SharedPreferences> { provideSharedPreferences(context = androidApplication()) }
    single<SettingRepository> { SettingRepository.Impl(get(), get()) }
    single<LtcRepository> { LtcRepository.Impl(get(), get(), get(), get()) }
}

val viewModelModule = module {
    viewModelOf(::WelcomeViewModel)
    viewModelOf(::SettingsViewModel)
    viewModel { ReadyViewModel() }
    viewModel { InputWordsViewModel() }
    viewModel { SetPasscodeViewModel() }
    viewModel { UnLockViewModel(get()) }
    viewModel { YourSeedProveItViewModel() }
    viewModel { YourSeedWordsViewModel() }
    viewModel { ReceiveDialogViewModel(get(), get()) }
    viewModel { BuyLitecoinViewModel(get(), get()) }
}

val appModule = module {
    single<KeyStoreManager> { KeyStoreManager(get(), KeyStoreKeyGenerator.Impl()) }
    single<CurrencyUpdateWorker> { CurrencyUpdateWorker(get()) }
}

private fun provideSharedPreferences(
    context: Context,
    name: String = "${BuildConfig.APPLICATION_ID}.prefs"
): SharedPreferences {
    return context.getSharedPreferences(name, Context.MODE_PRIVATE)
}

private fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
    .addInterceptor { chain ->
        val requestBuilder = chain.request()
            .newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Litecoin-Testnet", "false")
            .addHeader("Accept-Language", "en")
        chain.proceed(requestBuilder.build())
    }
    .addInterceptor(HttpLoggingInterceptor().apply {
        setLevel(
            when {
                BuildConfig.DEBUG -> HttpLoggingInterceptor.Level.BODY
                else -> HttpLoggingInterceptor.Level.NONE
            }
        )
    })
    .build()

internal fun provideRetrofit(
    okHttpClient: OkHttpClient,
    baseUrl: String = BRConstants.BW_API_PROD_HOST
): Retrofit = Retrofit.Builder()
    .baseUrl(baseUrl)
    .client(okHttpClient)
    .addConverterFactory(
        json.asConverterFactory(
            "application/json; charset=UTF8".toMediaType()
        )
    )
    .build()

internal inline fun <reified T> provideApi(retrofit: Retrofit): T =
    retrofit.create(T::class.java)

inline fun <reified T> getKoinInstance(): T {
    return object : KoinComponent {
        val value: T by inject()
    }.value
}