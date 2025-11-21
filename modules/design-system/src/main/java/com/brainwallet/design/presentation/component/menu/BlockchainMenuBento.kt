package com.brainwallet.design.presentation.component.menu

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.brainwallet.design.R
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

/**
 * Blockchain settings menu item for bento rail.
 * Provides access to Litecoin blockchain and fee configuration.
 */
@Composable
fun BlockchainMenuBento(
    modifier: Modifier = Modifier,
    selectedFeeType: String = stringResource(R.string.design_menu_blockchain_fee_regular),
    onClick: () -> Unit = {}
) {
    BentoMenuBase(
        title = stringResource(R.string.design_menu_blockchain_title),
        description = stringResource(R.string.design_menu_blockchain_fee_type, selectedFeeType),
        modifier = modifier,
        onClick = onClick
    )
}

@PreviewLightDark
@Composable
fun BlockchainMenuBentoPreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        BlockchainMenuBento()
    }
}
