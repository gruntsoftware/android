package com.brainwallet.design.presentation.component.menu

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

/**
 * Language settings menu item for bento rail.
 * Provides access to language selection preferences.
 */
@Composable
fun LanguageMenuBento(
    modifier: Modifier = Modifier,
    selectedLanguage: String = "English",
    onClick: () -> Unit = {}
) {
    BentoMenuBase(
        title = "Language",
        description = selectedLanguage,
        modifier = modifier,
        onClick = onClick
    )
}

@PreviewLightDark
@Composable
fun LanguageMenuBentoPreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        LanguageMenuBento(
            selectedLanguage = "English"
        )
    }
}
