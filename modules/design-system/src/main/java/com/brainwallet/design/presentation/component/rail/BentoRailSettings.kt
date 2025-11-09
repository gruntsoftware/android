package com.brainwallet.design.presentation.component.rail

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.brainwallet.design.presentation.component.menu.BlockchainMenuBento
import com.brainwallet.design.presentation.component.menu.CurrencyMenuBento
import com.brainwallet.design.presentation.component.menu.GamesMenuBento
import com.brainwallet.design.presentation.component.menu.LanguageMenuBento
import com.brainwallet.design.presentation.component.menu.LockMenuBento
import com.brainwallet.design.presentation.component.menu.SecurityMenuBento
import com.brainwallet.design.presentation.component.menu.SocialMediaMenuBento
import com.brainwallet.design.presentation.component.menu.SupportMenuBento
import com.brainwallet.design.presentation.component.menu.SyncMetadataMenuBento
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

/**
 * Settings section component for the navigation rail.
 * Contains all settings menu items with bento-style design.
 */
@Composable
fun BentoRailSettings(
    modifier: Modifier = Modifier,
    shareAnalyticsEnabled: Boolean = false,
    selectedLanguage: String = "English",
    selectedCurrency: String = "USD",
    selectedFeeType: String = "Regular",
    syncDescription: String = "No sync metadata",
    onSecurityClick: () -> Unit = {},
    onLanguageClick: () -> Unit = {},
    onCurrencyClick: () -> Unit = {},
    onGamesClick: () -> Unit = {},
    onBlockchainClick: () -> Unit = {},
    onSupportClick: () -> Unit = {},
    onSocialMediaClick: () -> Unit = {},
    onLockClick: () -> Unit = {},
    onSyncMetadataClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SecurityMenuBento(
            shareAnalyticsEnabled = shareAnalyticsEnabled,
            onClick = onSecurityClick
        )

        LanguageMenuBento(
            selectedLanguage = selectedLanguage,
            onClick = onLanguageClick
        )

        CurrencyMenuBento(
            selectedCurrency = selectedCurrency,
            onClick = onCurrencyClick
        )

        GamesMenuBento(
            onClick = onGamesClick
        )

        BlockchainMenuBento(
            selectedFeeType = selectedFeeType,
            onClick = onBlockchainClick
        )

        SupportMenuBento(
            onClick = onSupportClick
        )

        SocialMediaMenuBento(
            onClick = onSocialMediaClick
        )

        LockMenuBento(
            onClick = onLockClick
        )

        SyncMetadataMenuBento(
            syncDescription = syncDescription,
            onClick = onSyncMetadataClick
        )
    }
}

@PreviewLightDark
@Composable
@Preview
fun BentoRailSettingsPreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BentoRailSettings(
                shareAnalyticsEnabled = true,
                selectedLanguage = "English",
                selectedCurrency = "USD",
                selectedFeeType = "Regular",
                syncDescription = "Last sync: 2 hours ago"
            )
        }
    }
}
