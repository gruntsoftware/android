package com.brainwallet.ui.screens.home
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.Language

data class SettingsState(
    val darkMode: Boolean = true,
    val selectedLanguage: Language = Language.ENGLISH,
    val selectedCurrency: CurrencyEntity = CurrencyEntity(
        "USD",
        "USD",
        -1f
    ), //-1 = need to fetch
    val languageSelectorBottomSheetVisible: Boolean = false,
    val fiatSelectorBottomSheetVisible: Boolean = false,
)