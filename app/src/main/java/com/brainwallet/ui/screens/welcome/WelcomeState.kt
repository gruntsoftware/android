package com.brainwallet.ui.screens.welcome

import com.brainwallet.data.model.Language
import com.brainwallet.data.model.CurrencyEntity

data class WelcomeState(
    val darkMode: Boolean = true,
    val selectedLanguage: Language = Language.ENGLISH,
    val selectedCurrency: CurrencyEntity = CurrencyEntity(
        "USD",
        "US Dollar",
        -1f,
        "$"
    ), //-1 = need to fetch
    val languageSelectorBottomSheetVisible: Boolean = false,
    val fiatSelectorBottomSheetVisible: Boolean = false,
)
