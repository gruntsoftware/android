package com.brainwallet.ui.bentosections.tutorials.walkthrough

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.ui.composable.CalloutWithPointers
import com.brainwallet.ui.theme.IBMPlexSans

@Composable
fun TutorialWalkthroughPage1(
    darkMode: Boolean,
    modifier: Modifier = Modifier
) {
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
            Spacer(modifier = Modifier.weight(0.2f))
            Row(
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                Spacer(modifier = Modifier.padding(40.dp))
                CalloutWithPointers(
                    title = stringResource(com.brainwallet.R.string.walkthrough_title_1),
                    subtitle = stringResource(com.brainwallet.R.string.walkthrough_description_1)

                )
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@PreviewLightDark
@Composable
private fun TutorialWalkthroughPage1Preview() {
    Column {
        TutorialWalkthroughPage1(darkMode = true)
        TutorialWalkthroughPage1(darkMode = false)
    }
}
