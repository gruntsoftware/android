package com.brainwallet.ui.bentosections.shopbento.cards

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.draw.rotate
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Alignment
import coil.compose.rememberAsyncImagePainter

@Composable
fun SingleCardComposable(
    rotation: Float,
    modelString: String,
    modifier: Modifier = Modifier,
) {
    val cornerRadius = 6.dp

    BoxWithConstraints(modifier = Modifier.fillMaxSize().rotate(rotation)) {
        val cardWidth = maxWidth * 0.7f
        val cardHeight = cardWidth / 1.586f

        Image(
            painter = rememberAsyncImagePainter(modelString),
            contentDescription = null,
            modifier = Modifier
                .size(width = cardWidth, height = cardHeight)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(cornerRadius))

        )
    }
}
