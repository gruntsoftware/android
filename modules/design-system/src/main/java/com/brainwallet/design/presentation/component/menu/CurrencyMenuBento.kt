package com.brainwallet.design.presentation.component.menu

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

/**
 * Currency settings menu item for bento rail.
 * Provides access to fiat currency selection preferences.
 */
@Composable
fun CurrencyMenuBento(
    modifier: Modifier = Modifier,
    selectedCurrency: String = "USD",
    onClick: () -> Unit = {}
) {
    BentoMenuBase(
        title = "Currency",
        description = selectedCurrency,
        modifier = modifier,
        onClick = onClick
    )
}

@PreviewLightDark
@Composable
fun CurrencyMenuBentoPreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        CurrencyMenuBento(
            selectedCurrency = "USD"
        )
    }
}
