package com.brainwallet.ui.bentosections.tutorials.walkthrough

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.appreview.InAppReviewService
import com.brainwallet.ui.composable.CalloutWithPointers
import com.brainwallet.ui.composable.Pointer
import com.brainwallet.ui.theme.IBMPlexSans
import org.koin.compose.koinInject

@Composable
fun TutorialWalkthroughPage3(
    darkMode: Boolean,
    modifier: Modifier = Modifier,
    inAppReviewService: InAppReviewService = koinInject()
) {
    LaunchedEffect(Unit) {
        inAppReviewService.showInAppReviewDialogIfNeeded()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = stringResource(com.brainwallet.R.string.brainwallet_walkthrough_title),
                color = if (darkMode) Color.White else Color.Black,
                modifier = Modifier.fillMaxWidth()
                    .padding(
                        top = 30.dp,
                        bottom = 60.dp
                    ),
                style = TextStyle(
                    fontFamily = IBMPlexSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 22.sp,
                    textAlign = TextAlign.Center,
                    shadow = Shadow(
                        color = Color.Black.copy(alpha = 0.4f),
                        offset = Offset(x = 4f, y = 4f),
                        blurRadius = 5f
                    )
                ),
            )

            Row(
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Spacer(modifier = Modifier.weight(1f))
                CalloutWithPointers(
                    pointer = Pointer.BOTTOM_LEFT,
                    title = stringResource(com.brainwallet.R.string.walkthrough_title_3),
                    subtitle = stringResource(com.brainwallet.R.string.walkthrough_description_3),
                    calloutWidth = 250.dp,
                )
            }
            Spacer(modifier = Modifier.weight(0.9f))

            Row(
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Spacer(modifier = Modifier.weight(1f))
                CalloutWithPointers(
                    pointer = Pointer.BOTTOM_RIGHT,
                    title = stringResource(com.brainwallet.R.string.walkthrough_title_3a),
                    subtitle = stringResource(com.brainwallet.R.string.walkthrough_description_3a),
                    calloutWidth = 230.dp,
                )
            }
            Spacer(modifier = Modifier.weight(0.2f))
        }
    }
}

@Preview
@Composable
private fun TutorialWalkthroughPage3Preview() {
    Column {
        TutorialWalkthroughPage3(darkMode = true)
    }
}
