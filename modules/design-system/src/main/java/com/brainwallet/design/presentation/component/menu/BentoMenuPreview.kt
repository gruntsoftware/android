package com.brainwallet.design.presentation.component.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

/**
 * Preview showcasing all bento menu components together.
 * Demonstrates the complete settings menu in bento style.
 */
@PreviewLightDark
@Composable
@Preview
fun BentoMenuCollectionPreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BrainwalletTheme.colors.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SecurityMenuBento(
                shareAnalyticsEnabled = true
            )

            LanguageMenuBento(
                selectedLanguage = "English"
            )

            CurrencyMenuBento(
                selectedCurrency = "USD"
            )

            GamesMenuBento()

            BlockchainMenuBento(
                selectedFeeType = "Regular"
            )

            SupportMenuBento()

            SocialMediaMenuBento()

            LockMenuBento()

            SyncMetadataMenuBento(
                syncDescription = "Last sync: 2 hours ago"
            )
        }
    }
}
