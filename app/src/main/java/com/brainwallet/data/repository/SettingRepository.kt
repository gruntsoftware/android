package com.brainwallet.data.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.Language
import com.brainwallet.tools.sqlite.CurrencyDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

interface SettingRepository {

    val settings: Flow<AppSetting>

    suspend fun save(setting: AppSetting)

    /**
     * provide replacement for LocaleHelper for now
     * - getCurrentLanguage
     * - updateCurrentLanguage
     *
     * e.g. used by legacy [com.brainwallet.presenter.language.ChangeLanguageBottomSheet]
     */
    fun getCurrentLanguage(): Language

    fun updateCurrentLanguage(languageCode: String)

    /**
     * provide for legacy base class BRActivity.java to know current dark mode
     */
    fun isDarkMode(): Boolean

    fun toggleDarkMode(isDarkMode: Boolean)

    class Impl(
        private val sharedPreferences: SharedPreferences,
        private val currencyDataSource: CurrencyDataSource
    ) : SettingRepository {

        private val _state = MutableStateFlow(load())
        val state: StateFlow<AppSetting> = _state.asStateFlow()

        override val settings: Flow<AppSetting>
            get() = state

        override suspend fun save(setting: AppSetting) {
            sharedPreferences.edit {
                putBoolean(KEY_IS_DARK_MODE, setting.isDarkMode)
                putString(KEY_LANGUAGE_CODE, setting.languageCode)
                putString(KEY_FIAT_CURRENCY_CODE, setting.currency.code)
            }
            _state.update { setting }
        }

        override fun getCurrentLanguage(): Language {
            return sharedPreferences.getString(KEY_LANGUAGE_CODE, Language.ENGLISH.code)
                .let { languageCode -> Language.find(languageCode) }
        }

        override fun updateCurrentLanguage(languageCode: String) {
            sharedPreferences.edit { putString(KEY_LANGUAGE_CODE, languageCode) }
            _state.update { it.copy(languageCode = languageCode) }
        }

        override fun isDarkMode(): Boolean {
            return sharedPreferences.getBoolean(KEY_IS_DARK_MODE, true)
        }

        override fun toggleDarkMode(isDarkMode: Boolean) {
            sharedPreferences.edit {
                putBoolean(KEY_IS_DARK_MODE, isDarkMode)
            }
            _state.update { it.copy(isDarkMode = isDarkMode) }
        }

        private fun load(): AppSetting {
            return AppSetting(
                isDarkMode = sharedPreferences.getBoolean(KEY_IS_DARK_MODE, true),
                languageCode = sharedPreferences.getString(KEY_LANGUAGE_CODE, Language.ENGLISH.code)
                    ?: Language.ENGLISH.code,
                currency = sharedPreferences.getString(KEY_FIAT_CURRENCY_CODE, "USD").let {
                    currencyDataSource.getCurrencyByIso(it)
                        ?: return@let CurrencyEntity(
                            "USD",
                            "USD",
                            -1f
                        )
                }

            )
        }
    }

    companion object {
        const val KEY_IS_DARK_MODE = "is_dark_mode"
        const val KEY_LANGUAGE_CODE = "language_code"
        const val KEY_FIAT_CURRENCY_CODE = "fiat_currency_code"
    }
}