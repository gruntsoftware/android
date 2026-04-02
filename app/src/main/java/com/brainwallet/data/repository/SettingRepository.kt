package com.brainwallet.data.repository

import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.model.Language
import kotlinx.coroutines.flow.Flow

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

    fun putSelectedFeeType(feeType: String)

    fun getSelectedFeeType(): String

    companion object {
        const val KEY_IS_DARK_MODE = "is_dark_mode"
        const val KEY_LANGUAGE_CODE = "language_code"
        const val KEY_FIAT_CURRENCY_CODE = "fiat_currency_code"
        const val KEY_SELECTED_FEE_TYPE = "selected_fee_type"
        const val KEY_FIAT_CURRENCY_NAME = "selected_currency_name"
        const val KEY_FIAT_CURRENCY_RATE = "selected_currency_rate"
        const val KEY_FIAT_CURRENCY_SYMBOL = "selected_currency_symbol"
    }
}
