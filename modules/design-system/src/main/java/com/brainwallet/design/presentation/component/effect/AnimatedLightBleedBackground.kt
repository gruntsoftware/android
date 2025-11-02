package com.brainwallet.design.presentation.component.effect

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.grunt.brainwallet.core.presentation.theme.LocalDarkModeFlag
import com.grunt.brainwallet.core.presentation.theme.blue
import com.grunt.brainwallet.core.presentation.theme.grape
import com.grunt.brainwallet.core.presentation.theme.lavender
import com.grunt.brainwallet.core.presentation.theme.midnight
import com.grunt.brainwallet.core.presentation.theme.nearBlack
import com.grunt.brainwallet.core.presentation.theme.white
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated background with natural light effects that adapts to theme changes.
 * Creates realistic lighting from the top-right corner with smooth theme transitions.
 */
@Composable
fun AnimatedLightBleedBackground(
    modifier: Modifier = Modifier,
    animationDurationMs: Int = 8000,
    bleedIntensity: Float = 0.15f
) {
    val infiniteTransition = rememberInfiniteTransition(label = "lightBleedTransition")
    val isDarkMode = LocalDarkModeFlag.current

    val animationProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = animationDurationMs,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "lightBleedProgress"
    )

    val themeTransition by animateFloatAsState(
        targetValue = if (isDarkMode) 1f else 0f,
        animationSpec = tween(
            durationMillis = 800,
            easing = FastOutSlowInEasing
        ),
        label = "themeTransition"
    )

    val darkColors = remember(bleedIntensity) {
        createDarkModeColors(bleedIntensity)
    }

    val lightColors = remember(bleedIntensity) {
        createLightModeColors(bleedIntensity)
    }

    Canvas(
        modifier = modifier.fillMaxSize()
    ) {
        drawAnimatedLightBleed(
            animationProgress = animationProgress,
            darkColors = darkColors,
            lightColors = lightColors,
            themeTransition = themeTransition
        )
    }
}

private fun createDarkModeColors(bleedIntensity: Float): List<Color> {
    return listOf(
        blue.copy(alpha = bleedIntensity * 1.8f),
        midnight.copy(alpha = bleedIntensity * 1.2f),
        grape.copy(alpha = bleedIntensity * 0.8f),
        midnight.copy(alpha = bleedIntensity * 0.6f),
        nearBlack.copy(alpha = bleedIntensity * 0.4f),
        Color.Transparent,
        Color.Transparent
    )
}

private fun createLightModeColors(bleedIntensity: Float): List<Color> {
    return listOf(
        white.copy(alpha = bleedIntensity * 2.2f),
        lavender.copy(alpha = bleedIntensity * 1.5f),
        white.copy(alpha = bleedIntensity * 1.8f),
        lavender.copy(alpha = bleedIntensity * 1.0f),
        blue.copy(alpha = bleedIntensity * 0.6f),
        Color.Transparent,
        Color.Transparent
    )
}

private fun DrawScope.drawAnimatedLightBleed(
    animationProgress: Float,
    darkColors: List<Color>,
    lightColors: List<Color>,
    themeTransition: Float
) {
    val darkBase = Color.Black.copy(alpha = 1.0f)
    val lightBase = lavender.copy(alpha = 0.3f)
    val baseColor = Color(
        red = darkBase.red + (lightBase.red - darkBase.red) * (1f - themeTransition),
        green = darkBase.green + (lightBase.green - darkBase.green) * (1f - themeTransition),
        blue = darkBase.blue + (lightBase.blue - darkBase.blue) * (1f - themeTransition),
        alpha = darkBase.alpha + (lightBase.alpha - darkBase.alpha) * (1f - themeTransition)
    )

    drawRect(
        color = baseColor,
        size = size
    )

    val colors = if (themeTransition > 0.5f) darkColors else lightColors
    drawNaturalLight(animationProgress, colors)
}

private fun DrawScope.drawNaturalLight(
    animationProgress: Float,
    colors: List<Color>
) {
    val lightSourceX = size.width * 0.85f
    val lightSourceY = size.height * 0.15f

    val animatedX = lightSourceX + cos(animationProgress * 2 * Math.PI).toFloat() * size.width * 0.02f
    val animatedY = lightSourceY + sin(animationProgress * 2 * Math.PI * 0.7f).toFloat() * size.height * 0.015f

    val lightSources = listOf(
        Offset(animatedX, animatedY),
        Offset(animatedX - size.width * 0.05f, animatedY + size.height * 0.03f),
        Offset(animatedX + size.width * 0.03f, animatedY - size.height * 0.02f)
    )

    lightSources.forEachIndexed { index, lightSource ->
        val radiusMultiplier = when (index) {
            0 -> 1.2f
            1 -> 0.8f
            else -> 0.6f
        }

        val intensityMultiplier = when (index) {
            0 -> 1.0f
            1 -> 0.6f
            else -> 0.4f
        }

        val radius = size.width * radiusMultiplier
        val naturalColors = colors.map { color ->
            color.copy(alpha = color.alpha * intensityMultiplier)
        }

        val brush = Brush.radialGradient(
            colors = naturalColors,
            center = lightSource,
            radius = radius
        )

        drawRect(
            brush = brush,
            size = size
        )
    }
}
