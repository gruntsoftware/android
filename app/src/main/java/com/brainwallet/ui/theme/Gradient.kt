package com.brainwallet.ui.theme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode

val colorWhite = Color(0xFFFFFFFF)
val colorBlue = Color(0xFF114CD4)
val color80PercentMidnite = Color(0x800F0853) // 80%
val colorMidnite = Color(0xFF0F0853)
val colorSemiWhite = Color(0x80FFFFFF)
val colorLightWhite = Color(0x08FFFFFF)

// / Main Screen Background Colors
val colorMainScreenBackground1 = Color(0xFF0F0853)
val colorMainScreenBackground2 = Color(0xFF121348)
val colorMainScreenBackground3 = Color(0xFF491FA3)
val colorMainScreenBackground4 = Color(0xFF000000)
val colorMainScreenBackground5 = Color(0xFF000000)

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
            colorLightWhite,
            colorLightWhite
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
val mainScreenDarkSurfaceGradient: Brush
    @Composable
    get() = Brush.verticalGradient(
        0.0f to colorMainScreenBackground1,
        0.35f to colorMainScreenBackground2,
        0.70f to colorMainScreenBackground3,
        0.8f to colorMainScreenBackground4,
        1.0f to colorMainScreenBackground5,
        startY = 0f,
        endY = Float.POSITIVE_INFINITY
    )

val mainScreenLightSurfaceGradient: Brush
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
