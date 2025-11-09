package com.brainwallet.design.presentation.component.menu

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

/**
 * Blockchain settings menu item for bento rail.
 * Provides access to Litecoin blockchain and fee configuration.
 */
@Composable
fun BlockchainMenuBento(
    modifier: Modifier = Modifier,
    selectedFeeType: String = "Regular",
    onClick: () -> Unit = {}
) {
    BentoMenuBase(
        title = "Blockchain",
        description = "Fee type: $selectedFeeType",
        modifier = modifier,
        onClick = onClick
    )
}

@PreviewLightDark
@Composable
fun BlockchainMenuBentoPreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        BlockchainMenuBento(
            selectedFeeType = "Regular"
        )
    }
}
