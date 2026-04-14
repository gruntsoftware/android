package com.brainwallet.ui.bentosections.transactionbento.subscreens
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import com.brainwallet.ui.theme.DesignTheme

@Composable
fun ConfirmationStatus(
    modifier: Modifier = Modifier,
    numberOfConfs: Int = 0
) {
    val initialStateColor = DesignTheme.colors.info.copy(0.6f)
    val medianStateColor = DesignTheme.colors.affirm.copy(0.6f)
    val completeStateColor = DesignTheme.colors.affirm

    val filledColor = when (numberOfConfs) {
        in 3..4 -> medianStateColor
        in 5..Int.MAX_VALUE -> completeStateColor
        else -> initialStateColor
    }

    val emptyColor = DesignTheme.colors.info.copy(0.1f)

    val segmentColors = List(6) { i ->
        if (i < numberOfConfs.coerceIn(0, 6)) filledColor else emptyColor
    }

    Canvas(modifier = modifier.aspectRatio(1f)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.width / 2f * 0.9f

        val vertices = (0..5).map { i ->
            val angle = Math.toRadians(60.0 * i + 90.0)
            Offset(
                cx + r * cos(angle).toFloat(),
                cy - r * sin(angle).toFloat()
            )
        }

        vertices.forEachIndexed { i, v ->
            val next = vertices[(i + 1) % 6]
            val path = Path().apply {
                moveTo(cx, cy)
                lineTo(v.x, v.y)
                lineTo(next.x, next.y)
                close()
            }
            drawPath(path, color = segmentColors[i])
            drawPath(path, color = Color.White.copy(0.3f), style = Stroke(width = 1f))
        }
    }
}

class ConfirmationStateProvider : PreviewParameterProvider<Int> {
    override val values = sequenceOf(0, 1, 2, 3, 4, 5, 6, 7)
}

@Preview
@Composable
private fun ConfirmationStatusPreview(
    @PreviewParameter(ConfirmationStateProvider::class) numberOfConfs: Int
) {
    DesignTheme(isSystemInDarkTheme()) {
        Box(modifier = Modifier.height(120.dp)) {
            ConfirmationStatus(numberOfConfs = numberOfConfs)
        }
    }
}
