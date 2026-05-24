package com.brainwallet.ui.bentosections.shopbento.cards

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.brainwallet.ui.theme.giftCardGradient2
import com.brainwallet.R
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.rememberAsyncImagePainter
import com.brainwallet.ui.theme.giftCardGradient1
import com.brainwallet.ui.theme.giftCardGradient3

@Composable
fun SingleCardComposable(
    rotation: Float,
    modelString: String,
    offset: Offset,
    modifier: Modifier = Modifier,
) {
    val cornerRadius = 4.dp

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = offset.x.dp, y = offset.y.dp)
                .clip(RoundedCornerShape(cornerRadius))
        ) {
            rotate(degrees = 20f) {
                val cardWidth = size.width * 0.7f
                val cardHeight = cardWidth / 1.586f
                drawRoundRect(
                    brush = giftCardGradient2(size),
                    size = Size(cardWidth, cardHeight),
                    topLeft = Offset(
                        x = (size.width - cardWidth) / 2f,
                        y = (size.height - cardHeight) / 2f
                    ),
                    cornerRadius = CornerRadius(cornerRadius.toPx())
                )
            }
        }
        Image(
            painter = rememberAsyncImagePainter(modelString),
            contentDescription = null,
            modifier = Modifier
                .size(50.dp)
                .align(Alignment.Center)
                .offset(x = 30.dp, y = -10.dp)
                .rotate(rotation)
        )
    }
}

@Composable
fun CardAmazonComposable(
    rotation: Float,
    modifier: Modifier = Modifier,
) {
    val cornerRadius = 4.dp
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = 30.dp, y = -10.dp)
                .clip(RoundedCornerShape(cornerRadius))
        ) {
            rotate(degrees = 20f) {
                val cardWidth = size.width * 0.7f
                val cardHeight = cardWidth / 1.586f
                drawRoundRect(
                    brush = giftCardGradient2(size),
                    size = Size(cardWidth, cardHeight),
                    topLeft = Offset(
                        x = (size.width - cardWidth) / 2f,
                        y = (size.height - cardHeight) / 2f
                    ),
                    cornerRadius = CornerRadius(cornerRadius.toPx())
                )
            }
        }
        Image(
            painter = painterResource(R.drawable.amazon_logo),
            contentDescription = null,
            modifier = Modifier
                .size(50.dp)
                .align(Alignment.Center)
                .offset(x = 30.dp, y = -10.dp)
                .rotate(20f)
        )
    }
}

@Composable
fun CardVisaComposable(
    rotation: Float,
    modifier: Modifier = Modifier,
) {
    val cornerRadius = 4.dp
    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(cornerRadius))
        ) {
            rotate(degrees = rotation) {
                val cardWidth = size.width * 0.7f
                val cardHeight = cardWidth / 1.586f
                drawRoundRect(
                    brush = giftCardGradient1(size),
                    size = Size(cardWidth, cardHeight),
                    topLeft = Offset(
                        x = (size.width - cardWidth) / 2f,
                        y = (size.height - cardHeight) / 2f
                    ),
                    cornerRadius = CornerRadius(cornerRadius.toPx())
                )
            }
        }

        Image(
            painter = painterResource(R.drawable.visa_logo),
            contentDescription = null,
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.Center)
                .rotate(rotation)

        )
    }
}

@Composable
fun CardJustEatComposable(
    rotation: Float,
    modifier: Modifier = Modifier,
) {
    val cornerRadius = 4.dp

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(cornerRadius))
        ) {
            rotate(degrees = rotation) {
                val cardWidth = size.width * 0.7f
                val cardHeight = cardWidth / 1.586f
                drawRoundRect(
                    brush = giftCardGradient3(size),
                    size = Size(cardWidth, cardHeight),
                    topLeft = Offset(
                        x = (size.width - cardWidth) / 2f,
                        y = (size.height - cardHeight) / 2f
                    ),
                    cornerRadius = CornerRadius(cornerRadius.toPx())
                )
            }
        }
        Image(
            painter = painterResource(R.drawable.just_eat_logo),
            contentDescription = null,
            modifier = Modifier
                .size(50.dp)
                .align(Alignment.Center)
                .rotate(rotation)
        )
    }
}

@Preview
@Composable
private fun CardJustEatComposablePreview() {
    Box(modifier = Modifier) {
        CardJustEatComposable(rotation = -30f)
    }
}

@Preview
@Composable
private fun CardAmazonComposablePreview() {
    Box(modifier = Modifier) {
        CardAmazonComposable(rotation = -30f)
    }
}

@Preview
@Composable
private fun CardVisaComposablePreview() {
    Box(modifier = Modifier) {
        CardVisaComposable(rotation = -30f)
    }
}
