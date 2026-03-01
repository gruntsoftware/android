package com.brainwallet.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode

/**
 * A reusable vertical gradient brush for text and backgrounds.33
 */
val gameTitleGradient: Brush
    @Composable
    get() = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFFFFF),
            Color(0xFFFFFFFF),
            Color(0xFF114CD4),
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
val gameHubBackgroundGradient: Brush

    @Composable
    get() = Brush.radialGradient(
        colors = listOf(
            Color(0x80000000),
            Color(0x4D5827E2)
        ),
        center = Offset(100 / 2.0f, 200 / 2.0f),
        radius = 100 / 2.0f,
        tileMode = TileMode.Clamp
    )
val bentoBorderGradient: Brush

    @Composable
    get() = Brush.verticalGradient(
        colors = listOf(
            Color(0x80FFFFFF),
            Color(0xCC9074FF),
            Color(0xFF020148),
            Color(0xCC2B193B),
            Color(0xCC6944BE)
        ),
        startY = 0f,
        endY = 100f
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
