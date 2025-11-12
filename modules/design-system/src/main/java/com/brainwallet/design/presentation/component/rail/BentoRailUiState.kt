package com.brainwallet.design.presentation.component.rail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun rememberBentoRailState(
    initialUserName: String = "Joseph Sanjaya",
    initialAppVersion: String = "v.X.X.X (XXXXXXXXXXXX)",
    initialShareAnalyticsEnabled: Boolean = false,
    initialSelectedLanguage: String = "English",
    initialSelectedCurrency: String = "USD",
    initialSelectedFeeType: String = "Regular",
    initialSyncDescription: String = "No sync metadata"
): BentoRailUiState {
    return remember {
        BentoRailUiState(
            initialUserName = initialUserName,
            initialAppVersion = initialAppVersion,
            initialShareAnalyticsEnabled = initialShareAnalyticsEnabled,
            initialSelectedLanguage = initialSelectedLanguage,
            initialSelectedCurrency = initialSelectedCurrency,
            initialSelectedFeeType = initialSelectedFeeType,
            initialSyncDescription = initialSyncDescription
        )
    }
}

@Stable
class BentoRailUiState(
    initialUserName: String = "Joseph Sanjaya",
    initialAppVersion: String = "v.X.X.X (XXXXXXXXXXXX)",
    initialShareAnalyticsEnabled: Boolean = false,
    initialSelectedLanguage: String = "English",
    initialSelectedCurrency: String = "USD",
    initialSelectedFeeType: String = "Regular",
    initialSyncDescription: String = "No sync metadata"
) {
    var userName by mutableStateOf(initialUserName)
        private set

    var appVersion by mutableStateOf(initialAppVersion)
        private set

    var shareAnalyticsEnabled by mutableStateOf(initialShareAnalyticsEnabled)
        private set

    var selectedLanguage by mutableStateOf(initialSelectedLanguage)
        private set

    var selectedCurrency by mutableStateOf(initialSelectedCurrency)
        private set

    var selectedFeeType by mutableStateOf(initialSelectedFeeType)
        private set

    var syncDescription by mutableStateOf(initialSyncDescription)
        private set

    fun updateUserName(name: String) {
        userName = name
    }

    fun updateAppVersion(version: String) {
        appVersion = version
    }

    fun updateShareAnalyticsEnabled(enabled: Boolean) {
        shareAnalyticsEnabled = enabled
    }

    fun updateSelectedLanguage(language: String) {
        selectedLanguage = language
    }

    fun updateSelectedCurrency(currency: String) {
        selectedCurrency = currency
    }

    fun updateSelectedFeeType(feeType: String) {
        selectedFeeType = feeType
    }

    fun updateSyncDescription(description: String) {
        syncDescription = description
    }
}
