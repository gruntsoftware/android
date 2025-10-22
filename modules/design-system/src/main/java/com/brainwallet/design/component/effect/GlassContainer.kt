package com.brainwallet.design.component.effect

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

/**
 * Container that provides glass effect with background blur while keeping content sharp.
 * Uses layered approach with separate background and content layers.
 */
@Composable
fun GlassContainer(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    blurRadius: Dp = 12.dp,
    backgroundAlpha: Float = 0.15f,
    borderAlpha: Float = 0.3f,
    borderWidth: Dp = 1.dp,
    content: @Composable () -> Unit
) {
    val surfaceColor = BrainwalletTheme.colors.surface
    val borderColor = BrainwalletTheme.colors.border

    val glassBrush = remember(surfaceColor, backgroundAlpha) {
        createGlassGradient(surfaceColor, backgroundAlpha)
    }

    val borderBrush = remember(borderColor, borderAlpha) {
        createGlassBorderGradient(borderColor, borderAlpha)
    }

    Box(modifier = modifier.clip(shape)) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .then(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Modifier.blur(
                            radius = blurRadius,
                            edgeTreatment = BlurredEdgeTreatment(shape)
                        )
                    } else {
                        Modifier
                    }
                )
                .background(
                    brush = glassBrush,
                    shape = shape
                )
                .border(
                    width = borderWidth,
                    brush = borderBrush,
                    shape = shape
                )
        )

        content()
    }
}

private fun createGlassGradient(
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

private fun createGlassBorderGradient(
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
