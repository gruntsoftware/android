package com.brainwallet.ui.composable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect

@Composable
fun DashedDivider(
    modifier: Modifier = Modifier,
    color: Color = Color.White.copy(alpha = 0.3f),
    dashWidth: Dp = 8.dp,
    gapWidth: Dp = 4.dp,
    strokeWidth: Dp = 1.dp
) {
    val density = LocalDensity.current
    Canvas(
        modifier = modifier
            .height(strokeWidth)
    ) {
        val dashPx = with(density) { dashWidth.toPx() }
        val gapPx = with(density) { gapWidth.toPx() }
        val strokePx = with(density) { strokeWidth.toPx() }

        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            strokeWidth = strokePx,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashPx, gapPx), 0f)
        )
    }
}
