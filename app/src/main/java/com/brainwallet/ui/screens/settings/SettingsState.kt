package com.brainwallet.ui.screens.settings

import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.Fee
import com.brainwallet.data.model.FeeOption
import com.brainwallet.data.model.Language
import com.brainwallet.data.model.toFeeOptions
import com.brainwallet.tools.manager.FeeManager

import com.brainwallet.data.repository.SyncAnalyticsRepository

data class SettingsState(
    val darkMode: Boolean = true,
    val selectedLanguage: Language = Language.ENGLISH,
    val selectedCurrency: CurrencyEntity = CurrencyEntity(
        "USD",
        "US Dollar",
        -1f,
        "$"
    ), // -1 = need to fetch
    val languageSelectorBottomSheetVisible: Boolean = false,
    val brainwalletPhraseBottomSheetVisible: Boolean = false,
    val fiatSelectorBottomSheetVisible: Boolean = false,
    val shareAnalyticsDataEnabled: Boolean = false,
    val lastSyncMetadata: SyncAnalyticsRepository.SyncMetadata? = null,
    val currentFeeOptions: List<FeeOption> = Fee.Default.toFeeOptions(),
    val selectedFeeType: String = FeeManager.LUXURY,
    val formattedVersion: String = "",
    /** Raw user-set trusted LTC node address ("host" or "host:port"); null when none is set. */
    val trustedNodeAddress: String? = null
)
