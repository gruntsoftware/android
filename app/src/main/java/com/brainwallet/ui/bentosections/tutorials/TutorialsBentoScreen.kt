package com.brainwallet.ui.bentosections.tutorials

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brainwallet.tools.manager.AnalyticsManager
import com.brainwallet.ui.screens.main.MainViewModel
import com.brainwallet.ui.theme.DesignTheme
import com.brainwallet.ui.theme.IBMPlexSans
import com.brainwallet.ui.theme.mainBentoSurface
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TutorialsBentoScreen(
    modifier: Modifier = Modifier,
    onClick: (Int) -> Unit = {},
    viewModel: MainViewModel = koinViewModel()
) {
    val hubPageCount = 2
    val pagerState = rememberPagerState(pageCount = { hubPageCount })
    val appSetting by viewModel.appSetting.collectAsStateWithLifecycle()
    val isDarkMode = appSetting.isDarkMode

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .mainBentoSurface(isDarkMode)
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = stringResource(com.brainwallet.R.string.tutorials_label).uppercase(),
                color = if (isDarkMode) Color.White else Color.Black,
                style = TextStyle(
                    fontFamily = IBMPlexSans,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp
                ),
                modifier = Modifier
                    .padding(start = 1.dp, end = 1.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
        }
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = true,
            pageSpacing = 8.dp,
            modifier = modifier.fillMaxSize()
                .background(Color.Transparent)
        ) { page ->

            when (page) {
                0 -> TutorialWalkthroughBentoScreen(
                    onClick = {
                        AnalyticsManager.logCustomAdHocEvent("user_tapped_walkthrough_tutorial", null)
                        onClick(page)
                    }
                )
                1 -> TutorialSendBentoScreen(onClick = {
                    AnalyticsManager.logCustomAdHocEvent("user_tapped_send_tutorial", null)
                    onClick(page)
                })
            }
        }

        Row(
            Modifier
                .height(32.dp)
                .fillMaxWidth(0.35f)
                .padding(bottom = 8.dp)
                .background(color = Color.Black.copy(0.4f), shape = RoundedCornerShape(16.dp))
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) Color.White else DesignTheme.colors.background
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(8.dp)
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun TutorialsBentoScreenPreview() {
    Box(modifier = Modifier) {
        TutorialsBentoScreen()
    }
}
