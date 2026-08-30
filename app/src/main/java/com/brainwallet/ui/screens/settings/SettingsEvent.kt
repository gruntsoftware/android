package com.brainwallet.ui.screens.settings

import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.Language

import com.brainwallet.data.repository.SyncAnalyticsRepository

sealed class SettingsEvent {
    data class OnLoad(
        val shareAnalyticsDataEnabled: Boolean = false,
        val lastSyncMetadata: SyncAnalyticsRepository.SyncMetadata? = null,
    ) : SettingsEvent()
    object OnSecurityUpdatePinClick : SettingsEvent()
    object OnSecuritySeedPhraseClick : SettingsEvent()
    object OnSecurityBrainwalletPhraseClick : SettingsEvent()
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

    /** The trusted-LTC-node non-consumable was purchased from [LitecoinBlockchainDetail]'s IAP sheet. */
    object OnTrustedNodePurchased : SettingsEvent()

    /**
     * A trusted-node address ("host" or "host:port") the user entered in the set-trusted-node
     * sheet after purchasing. Already syntactically validated by the sheet; the ViewModel
     * re-checks with [com.brainwallet.tools.util.TrustedNode.isValid] before persisting.
     */
    data class OnTrustedNodeAddressSubmitted(val address: String) : SettingsEvent()
    data class OnFeeTypeChange(val feeType: String) : SettingsEvent()
}
