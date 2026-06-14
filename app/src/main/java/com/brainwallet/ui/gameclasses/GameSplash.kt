package com.brainwallet.ui.gameclasses

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.brainwallet.R
import com.brainwallet.ui.bentosections.gamehubbento.FallinScene

@Composable
fun GameSplash(modifier: Modifier = Modifier) {
    val gameHubBk = R.drawable.game_hub_bk
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(gameHubBk),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        FallinScene(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .align(Alignment.CenterEnd),
            dotQuantity = 24
        )
    }
}
