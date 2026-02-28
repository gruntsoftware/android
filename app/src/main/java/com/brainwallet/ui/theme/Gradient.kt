package com.brainwallet.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * A reusable vertical gradient brush for text and backgrounds.
 */
val gameTitleGradient: Brush
    @Composable
    get() = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFFFFFFFF),
            Color(0x33114CD4),
        )
    )
val gameTaglineGradient: Brush
    @Composable
    get() = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFFFFFFFF),
            Color(0xFFFFFFFF),
            Color(0x1A114CD4),
        )
    )

/**
 * Another example of a global gradient you could create.
 */
val primarySurfaceGradient: Brush
    @Composable
    get() = Brush.horizontalGradient(
        colors = listOf(
            // Example colors from a hypothetical theme
            // MaterialTheme.colorScheme.primary,
            // MaterialTheme.colorScheme.primaryContainer
            Color.Blue,
            Color.Cyan
        )
    )
