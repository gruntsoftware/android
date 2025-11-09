package com.brainwallet.design.presentation.component.menu

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

/**
 * Lock/unlock menu item for bento rail.
 * Provides access to app lock and security features.
 */
@Composable
fun LockMenuBento(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    BentoMenuBase(
        title = "Lock",
        description = "App security lock",
        modifier = modifier,
        onClick = onClick
    )
}

@PreviewLightDark
@Composable
fun LockMenuBentoPreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        LockMenuBento()
    }
}
