package com.brainwallet.ui.bentosections.shopbento

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.brainwallet.ui.bentosections.shopbento.cards.SingleCardComposable

@Composable
fun GiftCardsComposable(
    imageURLOne: String,
    imageURLTwo: String,
    imageURLThree: String,
    modifier: Modifier = Modifier,
) {
    var appeared by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        appeared = true
    }

    val offsetCardOne by animateFloatAsState(
        targetValue = if (appeared) 5f else 100f,
        animationSpec = tween(
            500,
            delayMillis = 400
        ),
        label = "cardOneSlide"
    )

    val offsetCardTwo by animateFloatAsState(
        targetValue = if (appeared) 30f else 100f,
        animationSpec = tween(
            600,
            delayMillis = 400
        ),
        label = "cardTwoSlide"
    )

    val offsetCardThree by animateFloatAsState(
        targetValue = if (appeared) 30f else 50f,
        animationSpec = tween(
            400,
            delayMillis = 400
        ),
        label = "cardThreeSlide"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()

    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .offset(x = offsetCardOne.dp, y = 5.dp)
        ) {
            SingleCardComposable(rotation = 20f, modelString = imageURLOne)
        }

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .offset(x = offsetCardTwo.dp, y = -20.dp)

        ) {
            SingleCardComposable(rotation = 20f, modelString = imageURLTwo)
        }

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .offset(x = offsetCardThree.dp, y = 35.dp)

        ) {
            SingleCardComposable(rotation = -30f, modelString = imageURLThree)
        }
    }
}
