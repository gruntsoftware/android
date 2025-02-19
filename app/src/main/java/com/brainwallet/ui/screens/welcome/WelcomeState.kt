package com.brainwallet.ui.screens.welcome

import com.brainwallet.data.model.Language
import com.brainwallet.presenter.entities.CurrencyEntity

data class WelcomeState(
    val darkMode: Boolean = true,
    val selectedLanguage: Language = Language.ENGLISH,
    val selectedCurrency: CurrencyEntity = CurrencyEntity("USD", "USD", -1f) //-1 = need to fetch
)
