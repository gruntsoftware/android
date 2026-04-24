package com.brainwallet.ui.bentosections.gamehubbento

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import kotlin.random.Random

public data class EmojiDot(
    val emoji: String,
    var x: Float,
    var y: Float,
    var velX: Float,
    var velY: Float
)

public val emojiList = listOf(
    "😬", "🇮🇹", "😂", "😭", "❤️", "🤣", "🔥",
    "😍", "🥺", "🥰", "🙏", "✨", "👀", "👠",
    "🍑", "🌴", "🍌", "🧄", "📻", "🏖️", "🎨",
    "💍", "🍋", "📡", "🌙", "🎭", "🦋", "🌸"
)

@Composable
fun FallinScene(
    modifier: Modifier = Modifier,
    dotQuantity: Int = 12,
) {
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val dots = remember { mutableStateListOf<EmojiDot>() }
    var lastTime = remember { 0L }
    var spawnTimer = remember { 0f }

    LaunchedEffect(Unit) {
        while (true) {
            withInfiniteAnimationFrameMillis { frameTimeMs ->
                val delta = if (lastTime == 0L) {
                    0.6f
                } else {
                    (frameTimeMs - lastTime) / 1000f
                }
                lastTime = frameTimeMs

                // spawn
                spawnTimer += delta
                if (spawnTimer >= 1f) {
                    spawnTimer = 0f
                    if (dots.size < dotQuantity) {
                        dots.add(
                            EmojiDot(
                                emoji = emojiList.random(),
                                x = Random.nextFloat(),
                                y = Random.nextFloat(),
                                velX = Random.nextFloat() * 100f - 50f,
                                velY = Random.nextFloat() * 60f - 30f
                            )
                        )
                    } else {
                        dots.clear()
                        dots.add(
                            EmojiDot(
                                emoji = emojiList.random(),
                                x = Random.nextFloat(),
                                y = Random.nextFloat(),
                                velX = Random.nextFloat() * 100f - 50f,
                                velY = Random.nextFloat() * 60f - 30f
                            )
                        )
                    }
                }

                // update physics
                val gravity = 1000f
                val restitution = 0.8f
                val friction = 0.04f

                dots.replaceAll { dot ->
                    var newVelY = dot.velY + gravity * delta
                    var newVelX = dot.velX * (1f - friction)
                    var newX = dot.x + newVelX * delta / 1000f
                    var newY = dot.y + newVelY * delta / 1000f

                    if (newY > 1f) {
                        newY = 1f
                        newVelY = -newVelY * restitution
                    }
                    if (newY < 0f) {
                        newY = 0f
                        newVelY = -newVelY * restitution
                    }
                    if (newX > 1f) {
                        newX = 1f
                        newVelX = -newVelX * restitution
                    }
                    if (newX < 0f) {
                        newX = 0f
                        newVelX = -newVelX * restitution
                    }

                    dot.copy(x = newX, y = newY, velX = newVelX, velY = newVelY)
                }
            }
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
    ) {
        dots.forEach { dot ->
            val px = dot.x * size.width
            val py = dot.y * size.height
            drawText(
                textMeasurer = textMeasurer,
                text = dot.emoji,
                topLeft = Offset(px - 20f, py - 20f),
                style = TextStyle(fontSize = 28.sp),
            )
        }
    }
}
