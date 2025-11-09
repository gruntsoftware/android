package com.brainwallet.design.presentation.component.menu

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

/**
 * Games settings menu item for bento rail.
 * Provides access to games and entertainment features.
 */
@Composable
fun GamesMenuBento(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    BentoMenuBase(
        title = "Games",
        description = "Entertainment features",
        modifier = modifier,
        onClick = onClick
    )
}

@PreviewLightDark
@Composable
fun GamesMenuBentoPreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        GamesMenuBento()
    }
}
