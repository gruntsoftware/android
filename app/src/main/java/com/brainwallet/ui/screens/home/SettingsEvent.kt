package com.brainwallet.ui.screens.home

import android.content.Context
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.Language
import com.brainwallet.ui.screens.unlock.UnLockEvent

sealed class SettingsEvent {
    object OnToggleDarkMode : SettingsEvent()
    object OnToggleLock : SettingsEvent()
    object OnLanguageSelectorButtonClick : SettingsEvent()
    object OnLanguageSelectorDismiss : SettingsEvent()
    data class OnLanguageChange(val language: Language) : SettingsEvent()
    object OnFiatButtonClick : SettingsEvent()
    object OnFiatSelectorDismiss : SettingsEvent()
    data class OnFiatChange(val currency: CurrencyEntity) : SettingsEvent()
    data class OnUserDidStartSync(val context: Context) : SettingsEvent()
}