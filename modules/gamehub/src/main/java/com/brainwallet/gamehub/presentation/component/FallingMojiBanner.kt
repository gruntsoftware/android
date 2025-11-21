package com.brainwallet.gamehub.presentation.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.design.presentation.component.effect.MediumOpacityContainer
import com.brainwallet.design.presentation.component.widget.GridChip
import com.brainwallet.gamehub.R
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme
import kotlin.random.Random

@Composable
fun FallingMojiBanner(
    modifier: Modifier = Modifier
) {
    val DeepPurple = Color(0xFF120524)
    val BrightPurple = Color(0xFF2A0E55)

    MediumOpacityContainer(
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(DeepPurple, BrightPurple, DeepPurple)
                    )
                )
        ) {
            StarryBackground()

            Box(modifier = Modifier.fillMaxSize()) {
                FallingItem(
                    emoji = "🍊",
                    modifier = Modifier.align(Alignment.TopCenter).offset(x = 60.dp, y = 40.dp),
                    trailHeight = 80.dp
                )

                FallingItem(
                    emoji = "🍌",
                    modifier = Modifier.align(Alignment.BottomCenter).offset(x = 30.dp, y = (-10).dp),
                    trailHeight = 60.dp
                )

                FallingItem(
                    emoji = "🍍",
                    modifier = Modifier.align(Alignment.TopEnd).offset(x = (-20).dp, y = 25.dp),
                    trailHeight = 100.dp
                )

                FallingItem(
                    emoji = "🍎",
                    modifier = Modifier.align(Alignment.BottomEnd).offset(x = (-40).dp, y = (-10).dp),
                    trailHeight = 70.dp
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 24.dp, top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.padding(top = 32.dp)) {
                    Text(
                        text = stringResource(R.string.gamehub_fallingmoji_title),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.3f),
                                offset = Offset(0f, 4f),
                                blurRadius = 8f
                            )
                        )
                    )
                    Text(
                        text = stringResource(R.string.gamehub_fallingmoji_subtitle),
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
            GridChip(
                stringResource(R.string.gamehub_grid_chip_label),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            )
        }
    }
}

@Composable
private fun FallingItem(
    emoji: String,
    trailHeight: Dp,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.BottomCenter) {
        Box(
            modifier = Modifier
                .padding(bottom = 20.dp)
                .width(30.dp)
                .height(trailHeight)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.White.copy(alpha = 0.05f),
                            Color.White.copy(alpha = 0.15f)
                        )
                    )
                )
        )
        Text(
            text = emoji,
            fontSize = 28.sp,
            modifier = Modifier
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(50))
        )
    }
}

private data class StarSpec(
    val xRatio: Float,
    val yRatio: Float,
    val alpha: Float,
    val radius: Float
)

@Composable
private fun StarryBackground() {
    val stars = remember {
        List(25) {
            StarSpec(
                xRatio = Random.nextFloat(),
                yRatio = Random.nextFloat(),
                alpha = Random.nextFloat() * 0.5f + 0.1f, // Random opacity between 0.1 and 0.6
                radius = Random.nextFloat() * 3f + 1f // Random size
            )
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        stars.forEach { star ->
            drawCircle(
                color = Color.White.copy(alpha = star.alpha),
                radius = star.radius,
                center = Offset(
                    x = star.xRatio * width,
                    y = star.yRatio * height
                )
            )
        }
    }
}

@Composable
@Preview(widthDp = 400)
private fun FallingMojiBannerPreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        Box(Modifier.background(Color(0xFF121212)).padding(16.dp)) {
            FallingMojiBanner()
        }
    }
}
