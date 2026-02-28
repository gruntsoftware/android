package com.brainwallet.ui.bentosections.gamehubbento

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.brainwallet.R

@Composable
fun GameHubBentoScreen(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val gameHubBackground = R.drawable.game_hub_bk
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(gameHubBackground),
                contentDescription = "game_hub_background",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Text(
                text = "FALLINMOJI",
                color = Color.White,
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier
                    .zIndex(1f)
                    .padding(8.dp)
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun GameHubBentoScreenPreview() {
    Box(modifier = Modifier) {
        GameHubBentoScreen()
    }
}
