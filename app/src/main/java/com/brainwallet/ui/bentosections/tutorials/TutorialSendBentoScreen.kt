package com.brainwallet.ui.bentosections.tutorials
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.graphics.Color
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

@Composable
fun TutorialSendBentoScreen(
    modifier: Modifier = Modifier,
    tutorialBentoState: TutorialBentoState = TutorialBentoState(),
    onClick: () -> Unit = {}
) {
    val hubPageCount = 3
    val pagerState = rememberPagerState(pageCount = { hubPageCount })
    var resizedDescriptionFontSize by remember { mutableStateOf(12.sp) }

    Card(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent),
            contentAlignment = Alignment.BottomCenter

        ) {
            Image(
                painter = painterResource(R.drawable.send_tut_3x),
                contentDescription = "fruits_background",
                modifier = Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(bentoCornerRadius))
                    .offset(y = 30.dp),
                contentScale = ContentScale.Crop,
                alignment = Alignment.BottomCenter
            )
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = stringResource(com.brainwallet.R.string.send_tutorial_title),
                    color = if (tutorialBentoState.darkMode) Color.White else Color.Black,
                    modifier = Modifier
                        .padding(
                            top = 35.dp,
                            start = 12.dp,
                            end = 12.dp
                        ),
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Start
                    ),
                    maxLines = 2
                )

                Text(
                    text = stringResource(com.brainwallet.R.string.send_tutorial_description),
                    color = if (tutorialBentoState.darkMode) Color.White else Color.Black,
                    modifier = Modifier
                        .padding(
                            top = 10.dp,
                            start = 12.dp
                        ),
                    onTextLayout = { textLayoutResult ->
                        if (textLayoutResult.hasVisualOverflow) {
                            resizedDescriptionFontSize *= 0.96f
                        }
                    },
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Start
                    ),
                    maxLines = 6
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun TutorialSendBentoScreenPreview() {
    Box(modifier = Modifier) {
        TutorialSendBentoScreen()
    }
}
