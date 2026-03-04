package com.brainwallet.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
val LocalLanguageISOCode = staticCompositionLocalOf {
    "en"
}
val LocalIsDarkModeFlag = staticCompositionLocalOf {
    false
}

val LocalBWColors = staticCompositionLocalOf {
    BWColors()
}

@Composable
fun DesignTheme(
    isDarkMode: Boolean,
    languageCode: String = "en",
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val colors = if (isDarkMode) darkScheme else lightScheme

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDarkMode
        }
    }

    CompositionLocalProvider(
        LocalBWColors provides colors,
        LocalLanguageISOCode provides languageCode,
        LocalIsDarkModeFlag provides isDarkMode,
    ) {
        MaterialTheme(
            typography = BWTypography,
            content = content
        )
    }
}
private val darkScheme = BWColors(
    surface = midnight,
    background = grape,
    content = white,
    border = white,
    info = blue,
    affirm = pesto,
    warn = cheddar,
    error = chili
)

private val lightScheme = BWColors(
    surface = white,
    background = lavender,
    content = midnight,
    border = midnight,
    info = blue,
    affirm = pesto,
    warn = cheddar,
    error = chili
)

/**
 * define custom colors naming for brainwallet colors
 */
@Immutable
data class BWColors(
    val surface: Color = Color.Unspecified,
    val background: Color = Color.Unspecified,
    val content: Color = Color.Unspecified,
    val border: Color = Color.Unspecified,
    val info: Color = Color.Unspecified,
    val affirm: Color = Color.Unspecified,
    val warn: Color = Color.Unspecified,
    val error: Color = Color.Unspecified
)

object DesignTheme {
    val colors: BWColors
        @Composable
        get() = LocalBWColors.current

    val typography: Typography
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.typography

    val shapes: Shapes
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.shapes
}
