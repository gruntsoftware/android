package com.brainwallet.ui.bentosections.tutorials
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brainwallet.R
import com.brainwallet.ui.theme.IBMPlexSans
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TutorialWalkthroughBentoScreen(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    viewModel: TutorialsBentoViewModel = koinViewModel(),
) {
    val hubPageCount = 3
    val pagerState = rememberPagerState(pageCount = { hubPageCount })
    var resizedDescriptionFontSize by remember { mutableStateOf(12.sp) }
    val state by viewModel.state.collectAsStateWithLifecycle()

    Card(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Image(
                painter = painterResource(R.drawable.fruits3),
                contentDescription = "fruits_background",
                modifier = Modifier.fillMaxWidth(0.8f)
                    .padding(bottom = 12.dp),
                contentScale = ContentScale.Fit
            )
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = stringResource(com.brainwallet.R.string.brainwallet_walkthrough_title),
                    color = if (state.darkMode) Color.White else Color.Black,
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
                    text = stringResource(com.brainwallet.R.string.brainwallet_walkthrough_description),
                    color = if (state.darkMode) Color.White else Color.Black,
                    modifier = Modifier
                        .padding(
                            top = 10.dp,
                            start = 12.dp,
                            end = 12.dp
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
private fun TutorialGeneralBentoScreenPreview() {
    Box(modifier = Modifier) {
        TutorialWalkthroughBentoScreen(
            modifier = Modifier
        )
    }
}
