package com.brainwallet.design.presentation.component.menu

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

/**
 * Support menu item for bento rail.
 * Provides access to support documentation and help resources.
 */
@Composable
fun SupportMenuBento(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    BentoMenuBase(
        title = "Support",
        description = "brainwallet.co/support.html",
        modifier = modifier,
        onClick = onClick
    )
}

@PreviewLightDark
@Composable
fun SupportMenuBentoPreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        SupportMenuBento()
    }
}
