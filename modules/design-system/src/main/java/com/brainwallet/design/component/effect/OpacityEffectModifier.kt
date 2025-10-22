package com.brainwallet.design.component.effect

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

/**
 * Applies opacity effect styling to a modifier.
 * Creates elegant appearance with gradient background and border.
 */
fun Modifier.opacityEffect(
    shape: Shape = RoundedCornerShape(16.dp),
    backgroundAlpha: Float = 0.15f,
    borderAlpha: Float = 0.3f,
    borderWidth: Dp = 1.dp
): Modifier = composed {
    val surfaceColor = BrainwalletTheme.colors.surface
    val borderColor = BrainwalletTheme.colors.border

    val backgroundBrush = remember(surfaceColor, backgroundAlpha) {
        createBackgroundGradient(surfaceColor, backgroundAlpha)
    }

    val borderBrush = remember(borderColor, borderAlpha) {
        createBorderGradient(borderColor, borderAlpha)
    }

    this
        .background(
            brush = backgroundBrush,
            shape = shape
        )
        .border(
            width = borderWidth,
            brush = borderBrush,
            shape = shape
        )
        .clip(shape)
}

private fun createBackgroundGradient(
    baseColor: Color,
    alpha: Float
): Brush {
    return Brush.verticalGradient(
        colors = listOf(
            baseColor.copy(alpha = alpha * 1.1f),
            baseColor.copy(alpha = alpha),
            baseColor.copy(alpha = alpha)
        ),
        startY = 0f,
        endY = Float.POSITIVE_INFINITY
    )
}

private fun createBorderGradient(
    borderColor: Color,
    alpha: Float
): Brush {
    return Brush.verticalGradient(
        colors = listOf(
            borderColor.copy(alpha = alpha * 1.5f),
            borderColor.copy(alpha = alpha * 0.3f),
            borderColor.copy(alpha = alpha * 0.8f)
        ),
        startY = 0f,
        endY = Float.POSITIVE_INFINITY
    )
}
