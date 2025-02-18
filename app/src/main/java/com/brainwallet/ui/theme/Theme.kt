package com.brainwallet.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext

private val lightScheme = lightColorScheme(
    primary = primaryBackgroundLight,
    onPrimary = onPrimaryBackgroundLight,
    secondary = secondaryBackgroundLight,
    onSecondary = onSecondaryBackgroundLight,
    error = errorBackgroundLight,
    onError = onErrorBackgroundLight,
    background = infoBackgroundLight,
    onBackground = onInfoBackgroundLight,

    /// DEV Why do Material 3 FORCES us to use the whole struct!!
    /// This is insane
    /// I just want to map the color to custom meanings
    //    val affirmBackgroundLight = Color(0xFFFFFFFF)
    //    val onAffirmBackgroundLight = Color(0xFF25AA2C)
    //    val warnBackgroundLight = Color(0xFFFFFFFF)
    //    val onWarnBackgroundLight = Color(0xFFFFAE00)
    //    val primaryBackgroundWhite = Color(0xFFFFFFFF)
    //    val onPrimaryBackgroundWhite = Color(0xFF151515)
)

private val darkScheme = darkColorScheme(
    primary = primaryBackgroundDark,
    onPrimary = onPrimaryBackgroundDark,
    secondary = secondaryBackgroundDark,
    onSecondary = onSecondaryBackgroundDark,
    error = errorBackgroundDark,
    onError = onErrorBackgroundDark,
    background = infoBackgroundDark,
    onBackground = onInfoBackgroundDark,

    /// DEV Why do Material 3 FORCES us to use the whole struct!!
    /// This is insane
    /// I just want to map the color to custom meanings
    // val affirmBackgroundDark = Color(0xFF2968F2)
    // val onAffirmBackgroundDark = Color(0xFF25AA2C)
    // val warnBackgroundDark = Color(0xFF0F0853)
    // val onWarnBackgroundDark = Color(0xFFCE3025)
    // val primaryBackgroundNearBlack= Color(0xFF151515)
    // val onPrimaryBackgroundNearBlack = Color(0xFFFFFFFF)
)

@Immutable
data class ColorFamily(
    val color: Color,
    val onColor: Color,
    val colorContainer: Color,
    val onColorContainer: Color
)

val unspecified_scheme = ColorFamily(
    Color.Unspecified, Color.Unspecified, Color.Unspecified, Color.Unspecified
)

@Composable
fun BrainwalletAppTheme(
    darkTheme: Boolean = true, //TODO: will change this later, will be using toggle
//    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val colorScheme = when {
        darkTheme -> darkScheme
        else -> lightScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

//provide compose theme wrapper for transition
fun ComposeView.setContentWithTheme(content: @Composable () -> Unit) {
    setContent {
        BrainwalletAppTheme { content.invoke() }
    }
}