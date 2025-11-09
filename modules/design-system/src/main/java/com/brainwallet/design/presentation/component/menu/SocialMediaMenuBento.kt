package com.brainwallet.design.presentation.component.menu

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

/**
 * Social media menu item for bento rail.
 * Provides access to social media links and community resources.
 */
@Composable
fun SocialMediaMenuBento(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    BentoMenuBase(
        title = "Social Media",
        description = "linktr.ee/brainwallet",
        modifier = modifier,
        onClick = onClick
    )
}

@PreviewLightDark
@Composable
fun SocialMediaMenuBentoPreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        SocialMediaMenuBento()
    }
}
