package com.brainwallet.ui.screens.welcome

import android.content.Context
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.Language

sealed class WelcomeEvent {
    data class OnLoad(val context: Context) : WelcomeEvent()
    object OnToggleDarkMode : WelcomeEvent()
    object OnLanguageSelectorButtonClick : WelcomeEvent()
    object OnLanguageSelectorDismiss : WelcomeEvent()
    data class OnLanguageChange(val language: Language) : WelcomeEvent()
    object OnFiatButtonClick : WelcomeEvent()
    object OnFiatSelectorDismiss : WelcomeEvent()
    data class OnFiatChange(val currency: CurrencyEntity) : WelcomeEvent()
}