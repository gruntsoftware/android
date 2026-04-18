package com.brainwallet.ui.bentosections.gamehubbento

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun GameHubBentoPagerScreen(
    modifier: Modifier = Modifier,
    onClick: (Int) -> Unit = {}
) {
    val hubPageCount = 3
    val pagerState = rememberPagerState(pageCount = { hubPageCount })
    val context = LocalContext.current

    LaunchedEffect(pagerState.pageCount) {
        var forward = true
        while (true) {
            delay(15_000)
            if (!pagerState.isScrollInProgress) {
                val nextPage = if (forward) {
                    (pagerState.currentPage + 1).also {
                        if (it >= pagerState.pageCount - 1) forward = false
                    }
                } else {
                    (pagerState.currentPage - 1).also {
                        if (it <= 0) forward = true
                    }
                }
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.5.dp,
                color = Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            ),
    ) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = true,
            pageSpacing = 8.dp,
            modifier = modifier.fillMaxSize()
        ) { page ->

            when (page) {
                0 -> GameHubBentoScreen(onClick = { onClick(page) })
                1 -> GameHubBentoRiceScreen(onClick = { onClick(page) })
                2 -> GameHubBentoUnagiScreen(onClick = { onClick(page) })
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun GameHubBentoPagerScreenPreview() {
    Box(modifier = Modifier.height(120.dp)) {
        GameHubBentoPagerScreen()
    }
}
