package com.brainwallet.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.grunt.brainwallet.core.presentation.theme.AppTypography

val LocalLanguageISOCode = staticCompositionLocalOf {
    "en"
}
val LocalDarkModeFlag = staticCompositionLocalOf {
    false
}

@Composable
fun DesignTheme(
    isDarkMode: Boolean,
    languageCode: String = "en",
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalLanguageISOCode provides languageCode,
        LocalDarkModeFlag provides isDarkMode,
    ) {
        MaterialTheme(
            typography = AppTypography,
            content = content
        )
    }
}
