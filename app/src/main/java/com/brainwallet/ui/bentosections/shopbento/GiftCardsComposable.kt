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
import com.brainwallet.ui.bentosections.shopbento.cards.CardAmazonComposable
import com.brainwallet.ui.bentosections.shopbento.cards.CardVisaComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.brainwallet.ui.bentosections.shopbento.cards.SingleCardComposable

@Composable
fun GiftCardsComposable(
    state: ShopBentoState,
    modifier: Modifier = Modifier,
) {
    var appeared by remember { mutableStateOf(false) }

    var cards = state.shopCards
    LaunchedEffect(Unit) {
        appeared = true
    }

    val offsetCardOne by animateFloatAsState(
        targetValue = if (appeared) -10f else 100f,
        animationSpec = tween(
            500,
            delayMillis = 200
        ),
        label = "cardOneSlide"
    )

    val offsetCardTwo by animateFloatAsState(
        targetValue = if (appeared) 30f else 100f,
        animationSpec = tween(
            600,
            delayMillis = 200
        ),
        label = "cardTwoSlide"
    )

    val offsetCardThree by animateFloatAsState(
        targetValue = if (appeared) -12f else 50f,
        animationSpec = tween(
            400,
            delayMillis = 200
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
            CardVisaComposable(rotation = 20f)
        }

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .offset(x = offsetCardTwo.dp, y = -15.dp)

        ) {
            CardAmazonComposable(rotation = 20f)
        }

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .offset(x = offsetCardThree.dp, y = 15.dp)

        ) {
            SingleCardComposable(rotation = -30f, modelString = "", offset = Offset(0F, 0F))
        }
    }
}
