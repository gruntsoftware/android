package com.brainwallet.ui.bentosections.gamehubbento

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.R
import com.brainwallet.ui.theme.BoldenVan
import com.brainwallet.ui.theme.gameTaglineGradient
import com.brainwallet.ui.theme.gameTitleGradient

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
            Column(
                modifier = Modifier
            ) {
                Text(
                    text = "FALLINMOJI",
                    style = TextStyle(
                        brush = gameTitleGradient,
                        fontFamily = BoldenVan,
                        fontWeight = FontWeight.Normal,
                        fontSize = 50.sp,
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.5f),
                            offset = Offset(x = 4f, y = 4f),
                            blurRadius = 4f
                        )
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                )

                Text(
                    text = stringResource(R.string.game_hub_tagline),
                    style = TextStyle(
                        brush = gameTaglineGradient,
                        fontFamily = BoldenVan,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.5f),
                            offset = Offset(x = 4f, y = 4f),
                            blurRadius = 4f
                        )
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )
            }
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
