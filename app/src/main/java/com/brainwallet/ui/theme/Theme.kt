package com.brainwallet.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView


/**
 * define custom colors naming for brainwallet colors
 */
@Immutable
data class BrainwalletColors(
    val surface: Color = Color.Unspecified,
    val background: Color = Color.Unspecified,
    val content: Color = Color.Unspecified,
    val border: Color = Color.Unspecified,
    val info: Color = Color.Unspecified,
    val affirm: Color = Color.Unspecified,
    val warn: Color = Color.Unspecified,
    val error: Color = Color.Unspecified
)

private val darkColorScheme = BrainwalletColors(
    surface = midnight,
    background = grape,
    content = white,
    border = white,
    info = blue,
    affirm = pesto,
    warn = cheddar,
    error = chilli
)

private val lightColorScheme = BrainwalletColors(
    surface = white,
    background = lavender,
    content = midnight,
    border = midnight,
    info = blue,
    affirm = pesto,
    warn = cheddar,
    error = chilli
)

val LocalBrainwalletColors = staticCompositionLocalOf {
    BrainwalletColors()
}

@Composable
fun BrainwalletAppTheme(
    darkTheme: Boolean = true, //TODO: will change this later, will be using toggle
//    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable() () -> Unit
) {
    val colors = if (darkTheme) darkColorScheme else lightColorScheme

    CompositionLocalProvider(LocalBrainwalletColors provides colors) {
        MaterialTheme(
            typography = AppTypography,
            content = content
        )
    }
}

object BrainwalletTheme {
    val colors: BrainwalletColors
        @Composable
        get() = LocalBrainwalletColors.current

    //todo: add typography, shape? for the design system
}

//provide compose theme wrapper for transition
fun ComposeView.setContentWithTheme(content: @Composable () -> Unit) {
    setContent {
        BrainwalletAppTheme { content.invoke() }
    }
}