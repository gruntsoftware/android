package com.brainwallet.ui.theme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TileMode

val colorWhite = Color(0xFFFFFFFF)
val colorBlue = Color(0xFF114CD4)
val color80PercentMidnite = Color(0xFF0F0853).copy(alpha = 0.8f)
val colorMidnite = Color(0xFF0F0853)
val colorSemiWhite = Color(0xFFFFFFFF).copy(alpha = 0.8f)
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

val balanceBackgroundGradient: Brush

    @Composable
    get() = Brush.verticalGradient(
        0.0f to colorMainScreenBackground5,
        1.0f to colorMainScreenBackground1,
        startY = 0f,
        endY = Float.POSITIVE_INFINITY,
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
    get() = Brush.linearGradient(
        colors = listOf(
            Color(0xFFD9D9D9),
            Color(0xFFDfDfDf),
        ),
        start = Offset.Zero,
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
    )
val bentoClearGradient: Brush

    @Composable
    get() = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFFFFF).copy(alpha = 0.0f),
            Color(0xFFFFFFFF).copy(alpha = 0.0f)
        ),
        start = Offset(Float.MIN_VALUE, Float.POSITIVE_INFINITY),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )
val bentoDarkBorderGradient: Brush

    @Composable
    get() = Brush.linearGradient(
        colors = listOf(
            Color(0xFFFFFFFF).copy(alpha = 0.2f),
            Color(0xFF9074FF).copy(alpha = 0.8f),
            Color(0xFF020148).copy(alpha = 1f),
            Color(0xFF2B193B).copy(alpha = 1f),
            Color(0xFF6944BE).copy(alpha = 0.2f)
        ),
        start = Offset.Zero,
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )

val bentoModalDarkGradient: Brush

    @Composable
    get() = Brush.linearGradient(
        colors = listOf(
            Color(0xFF09082B).copy(alpha = 1f),
            Color(0xFF280589).copy(alpha = 0.7f)
        ),
        start = Offset(Float.MIN_VALUE, Float.POSITIVE_INFINITY),
        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
    )
