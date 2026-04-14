package com.brainwallet.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
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

            // Sets the color to transparent
            window.statusBarColor = Color.Transparent.toArgb()

            // Adjusts the icon colors (dark icons for light theme, light icons for dark theme)
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !isDarkMode
            insetsController.isAppearanceLightNavigationBars = !isDarkMode
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

fun Modifier.blurWhen(condition: Boolean): Modifier = this.graphicsLayer {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        renderEffect = if (condition) {
            BlurEffect(
                radiusX = 40f,
                radiusY = 40f,
                edgeTreatment = TileMode.Decal
            )
        } else {
            null
        }
    }
}

fun Modifier.blurAnimatedWith(radius: Float): Modifier = this.graphicsLayer {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        renderEffect = if (radius > 0f) {
            BlurEffect(
                radiusX = radius,
                radiusY = radius,
                edgeTreatment = TileMode.Decal
            )
        } else {
            null
        }
    }
}
