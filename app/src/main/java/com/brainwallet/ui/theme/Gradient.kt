package com.brainwallet.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode

val colorWhite = Color(0xFFFFFFFF)
val colorBlack = Color(0xFF000000)
val colorBlue = Color(0xFF114CD4)
val colorLightMidnite = Color(0x1A114CD4)
val colorSemiMidnite = Color(0xCC2B193B)
val colorPurple = Color(0xCC6944BE)
val colorLavender = Color(0xFF9074FF)
val colorGrayLight = Color(0xFFD9D9D9)
val colorMidnite = Color(0xFF020148)
val colorSemiWhite = Color(0x80FFFFFF)

/**
 * A reusable vertical gradient brush for text and backgrounds.33
 */
val gameTitleGradient: Brush
    @Composable
    get() = Brush.verticalGradient(
        colors = listOf(
            colorWhite,
            colorWhite,
            colorBlue,
        )
    )
val gameTaglineGradient: Brush
    @Composable
    get() = Brush.verticalGradient(
        colors = listOf(
            colorWhite,
            colorWhite,
            colorWhite,
            colorBlue,
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
val bentoDarkSurfaceGradient: Brush
    @Composable
    get() = Brush.verticalGradient(
        colors = listOf(
            colorMidnite,
            colorMidnite
        ),
        startY = 0f,
        endY = 100f
    )

val bentoLightSurfaceGradient: Brush
    @Composable
    get() = Brush.verticalGradient(
        colors = listOf(
            colorWhite,
            colorWhite
        ),
        startY = 0f,
        endY = 100f
    )
val bentoLightBorderGradient: Brush

    @Composable
    get() = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFD9D9D9),
            Color(0xFFD9D9D9),
        ),
        startY = 0f,
        endY = 100f
    )
val bentoDarkBorderGradient: Brush

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
