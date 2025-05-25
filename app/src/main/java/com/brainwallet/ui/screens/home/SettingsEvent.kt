package com.brainwallet.ui.screens.home

import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.Language

sealed class SettingsEvent {
    data class OnLoad(
        val shareAnalyticsDataEnabled: Boolean = false,
        val lastSyncMetadata: String? = null,
    ) : SettingsEvent()

    object OnSecurityUpdatePinClick : SettingsEvent()
    object OnSecuritySeedPhraseClick : SettingsEvent()
    object OnSecurityShareAnalyticsDataClick : SettingsEvent()
    object OnToggleDarkMode : SettingsEvent()
    object OnToggleLock : SettingsEvent()
    object OnLanguageSelectorButtonClick : SettingsEvent()
    object OnLanguageSelectorDismiss : SettingsEvent()
    data class OnLanguageChange(val language: Language) : SettingsEvent()
    object OnFiatButtonClick : SettingsEvent()
    object OnFiatSelectorDismiss : SettingsEvent()
    data class OnFiatChange(val currency: CurrencyEntity) : SettingsEvent()
    object OnBlockchainSyncClick : SettingsEvent()
    data class OnFeeTypeChange(val feeType: String) : SettingsEvent()
}