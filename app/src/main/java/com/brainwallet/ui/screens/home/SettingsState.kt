package com.brainwallet.ui.screens.home

import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.Fee
import com.brainwallet.data.model.FeeOption
import com.brainwallet.data.model.Language
import com.brainwallet.data.model.toFeeOptions
import com.brainwallet.tools.manager.FeeManager

data class SettingsState(
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
    val shareAnalyticsDataEnabled: Boolean = false,
    val lastSyncMetadata: String? = null,
    val currentFeeOptions: List<FeeOption> = Fee.Default.toFeeOptions(),
    val selectedFeeType: String = FeeManager.LUXURY
)