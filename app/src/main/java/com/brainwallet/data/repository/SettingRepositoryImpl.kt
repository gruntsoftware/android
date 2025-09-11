package com.brainwallet.data.repository

import android.content.SharedPreferences
import androidx.core.content.edit
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.Language
import com.brainwallet.data.repository.SettingRepository.Companion.KEY_FIAT_CURRENCY_CODE
import com.brainwallet.data.repository.SettingRepository.Companion.KEY_IS_DARK_MODE
import com.brainwallet.data.repository.SettingRepository.Companion.KEY_LANGUAGE_CODE
import com.brainwallet.data.repository.SettingRepository.Companion.KEY_SELECTED_FEE_TYPE
import com.brainwallet.tools.manager.FeeManager
import com.brainwallet.tools.sqlite.CurrencyDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

@Single(binds = [SettingRepository::class])
class SettingRepositoryImpl(
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

    override fun putSelectedFeeType(feeType: String) {
        sharedPreferences.edit {
            putString(KEY_SELECTED_FEE_TYPE, feeType)
        }
    }

    override fun getSelectedFeeType(): String =
        sharedPreferences.getString(KEY_SELECTED_FEE_TYPE, FeeManager.REGULAR)
            ?: FeeManager.REGULAR

    private fun load(): AppSetting {
        return AppSetting(
            isDarkMode = sharedPreferences.getBoolean(KEY_IS_DARK_MODE, true),
            languageCode = sharedPreferences.getString(KEY_LANGUAGE_CODE, Language.ENGLISH.code)
                ?: Language.ENGLISH.code,
            currency = sharedPreferences.getString(KEY_FIAT_CURRENCY_CODE, "USD").let {
                currencyDataSource.getCurrencyByIso(it)
                    ?: return@let CurrencyEntity(
                        "USD",
                        "US Dollar",
                        -1f,
                        "$"
                    )
            }

        )
    }
}
