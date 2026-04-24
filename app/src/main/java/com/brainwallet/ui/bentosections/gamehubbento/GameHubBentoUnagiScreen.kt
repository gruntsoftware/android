package com.brainwallet.ui.bentosections.gamehubbento
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.R
import com.brainwallet.constants.bentoCornerRadius
import com.brainwallet.ui.theme.IBMPlexSans
import com.brainwallet.ui.theme.balanceGameBentoSurface
import com.brainwallet.ui.theme.gameTaglineGradient

@Composable
fun GameHubBentoUnagiScreen(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val hubPageCount = 3
    val pagerState = rememberPagerState(pageCount = { hubPageCount })
    var resizedTaglineFontSize by remember { mutableStateOf(19.sp) }
    val unagiBackground = R.drawable.unagi_social_02

    Card(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .balanceGameBentoSurface(isDarkMode = true)

        ) {
            Image(
                painter = painterResource(unagiBackground),
                contentDescription = "game_hub_background",
                modifier = Modifier.fillMaxSize()
                    .clip(RoundedCornerShape(bentoCornerRadius)),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .padding(6.dp)
                        .offset(y = (-10).dp),
                    text = stringResource(R.string.unagi_social_01_tagline),

                    textAlign = TextAlign.Center,
                    onTextLayout = { textLayoutResult ->
                        if (textLayoutResult.hasVisualOverflow) {
                            resizedTaglineFontSize *= 0.95f
                        }
                    },
                    style = TextStyle(
                        brush = gameTaglineGradient,
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = resizedTaglineFontSize
                    ),
                    maxLines = 2
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun GameHubBentoUnagiScreenPreview() {
    Box(modifier = Modifier.height(120.dp)) {
        GameHubBentoUnagiScreen()
    }
}
