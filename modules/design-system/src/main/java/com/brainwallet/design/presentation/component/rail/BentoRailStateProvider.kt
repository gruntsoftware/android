package com.brainwallet.design.presentation.component.rail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.koin.compose.koinInject

@Composable
fun rememberBentoRailStateWithProvider(
    dataProvider: BentoRailDataProvider = koinInject()
): BentoRailUiState {
    val state = rememberBentoRailState(
        initialAppVersion = dataProvider.getAppVersion(),
        initialShareAnalyticsEnabled = dataProvider.getShareAnalyticsEnabled(),
        initialSelectedLanguage = dataProvider.getSelectedLanguage(),
        initialSelectedCurrency = dataProvider.getSelectedCurrency(),
        initialSelectedFeeType = dataProvider.getSelectedFeeType(),
        initialSyncDescription = dataProvider.getSyncDescription()
    )

    LaunchedEffect(Unit) {
        state.updateAppVersion(dataProvider.getAppVersion())
        state.updateShareAnalyticsEnabled(dataProvider.getShareAnalyticsEnabled())
        state.updateSelectedLanguage(dataProvider.getSelectedLanguage())
        state.updateSelectedCurrency(dataProvider.getSelectedCurrency())
        state.updateSelectedFeeType(dataProvider.getSelectedFeeType())
        state.updateSyncDescription(dataProvider.getSyncDescription())
    }

    return state
}
