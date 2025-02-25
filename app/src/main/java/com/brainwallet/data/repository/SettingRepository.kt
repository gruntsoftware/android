package com.brainwallet.data.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.model.Language
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

    class Impl(
        private val sharedPreferences: SharedPreferences
    ) : SettingRepository {

        private val _state = MutableStateFlow(load())
        val state: StateFlow<AppSetting> = _state.asStateFlow()

        override val settings: Flow<AppSetting>
            get() = state

        override suspend fun save(setting: AppSetting) {
            sharedPreferences.edit {
                putBoolean(KEY_IS_DARK_MODE, setting.isDarkMode)
                putString(KEY_LANGUAGE_CODE, setting.languageCode)
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

        private fun load(): AppSetting {
            return AppSetting(
                isDarkMode = sharedPreferences.getBoolean(KEY_IS_DARK_MODE, true),
                languageCode = sharedPreferences.getString(KEY_LANGUAGE_CODE, Language.ENGLISH.code)
                    ?: Language.ENGLISH.code
            )
        }
    }

    companion object {
        const val KEY_IS_DARK_MODE = "is_dark_mode"
        const val KEY_LANGUAGE_CODE = "language_code"
    }
}