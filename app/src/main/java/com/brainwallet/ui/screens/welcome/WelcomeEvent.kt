package com.brainwallet.ui.screens.welcome

import com.brainwallet.data.model.Language
import com.brainwallet.data.model.CurrencyEntity

sealed class WelcomeEvent {
    object OnToggleDarkMode : WelcomeEvent()
    object OnLanguageSelectorButtonClick : WelcomeEvent()
    object OnLanguageSelectorDismiss : WelcomeEvent()
    data class OnLanguageChange(val language: Language) : WelcomeEvent()
    object OnFiatButtonClick : WelcomeEvent()
    object OnFiatSelectorDismiss : WelcomeEvent()
    data class OnFiatChange(val currency: CurrencyEntity) : WelcomeEvent()
}