package com.brainwallet.design.presentation.component.menu

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

/**
 * Security settings menu item for bento rail.
 * Provides access to security and analytics sharing preferences.
 */
@Composable
fun SecurityMenuBento(
    modifier: Modifier = Modifier,
    shareAnalyticsEnabled: Boolean = false,
    onClick: () -> Unit = {}
) {
    BentoMenuBase(
        title = "Security",
        description = if (shareAnalyticsEnabled) "Analytics sharing enabled" else "Analytics sharing disabled",
        modifier = modifier,
        onClick = onClick
    )
}

@PreviewLightDark
@Composable
fun SecurityMenuBentoPreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        SecurityMenuBento(
            shareAnalyticsEnabled = true
        )
    }
}
