package com.brainwallet.design.presentation.component.menu

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.brainwallet.design.R
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

/**
 * Currency settings menu item for bento rail.
 * Provides access to fiat currency selection preferences.
 */
@Composable
fun CurrencyMenuBento(
    modifier: Modifier = Modifier,
    selectedCurrency: String = stringResource(R.string.design_menu_currency_default),
    onClick: () -> Unit = {}
) {
    BentoMenuBase(
        title = stringResource(R.string.design_menu_currency_title),
        description = selectedCurrency,
        modifier = modifier,
        onClick = onClick
    )
}

@PreviewLightDark
@Composable
fun CurrencyMenuBentoPreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        CurrencyMenuBento()
    }
}
