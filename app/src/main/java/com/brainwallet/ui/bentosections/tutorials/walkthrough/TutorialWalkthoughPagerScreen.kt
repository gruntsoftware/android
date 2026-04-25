package com.brainwallet.ui.bentosections.tutorials.walkthrough

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brainwallet.navigation.OnNavigate
import com.brainwallet.ui.bentosections.tutorials.TutorialsBentoViewModel
import com.brainwallet.ui.theme.DesignTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TutorialWalkthroughPagerScreen(
    onNavigate: OnNavigate,
    modifier: Modifier = Modifier,
    viewModel: TutorialsBentoViewModel = koinViewModel(),
    onDismissTutorialWalkthroughModal: () -> Unit = {},
) {
    val hubPageCount = 4
    val pagerState = rememberPagerState(pageCount = { hubPageCount })
    val state by viewModel.state.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = true,
            pageSpacing = 8.dp,
            modifier = modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> TutorialWalkthroughPage1(darkMode = state.darkMode)
                1 -> TutorialWalkthroughPage2(darkMode = state.darkMode)
                2 -> TutorialWalkthroughPage3(darkMode = state.darkMode)
                3 -> TutorialWalkthroughPage4(darkMode = state.darkMode)
            }
        }

        Row(
            Modifier
                .height(32.dp)
                .fillMaxWidth(0.25f)
                .padding(bottom = 16.dp)
                .background(
                    color = Color.Black.copy(0.4f),
                    shape = RoundedCornerShape(16.dp)
                )
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
