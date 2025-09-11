package com.brainwallet.di

import android.content.Context
import android.content.SharedPreferences
import com.brainwallet.BuildConfig
import com.brainwallet.data.source.RemoteApiSource
import com.brainwallet.tools.sqlite.CurrencyDataSource
import com.brainwallet.tools.util.BRConstants
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlin.getValue

@Module
@ComponentScan("com.brainwallet")
object AppModule {

    val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        prettyPrint = true
    }

    val dataModule = module {
        single { Firebase.remoteConfig }
        single { json }
        factory { provideOkHttpClient() }
        single { provideRetrofit(get(), get(), BRConstants.BW_API_PROD_HOST) }
        single { provideApi<RemoteApiSource>(get()) }
        single { CurrencyDataSource.getInstance(get()) }
        single<SharedPreferences> { provideSharedPreferences(context = androidApplication()) }
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
        .addInterceptor(
            HttpLoggingInterceptor().apply {
                setLevel(
                    when {
                        BuildConfig.DEBUG -> HttpLoggingInterceptor.Level.BODY
                        else -> HttpLoggingInterceptor.Level.NONE
                    }
                )
            }
        )
        .build()

    internal fun provideRetrofit(
        json: Json,
        okHttpClient: OkHttpClient,
        baseUrl: String = BRConstants.BW_API_PROD_HOST,
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
}
