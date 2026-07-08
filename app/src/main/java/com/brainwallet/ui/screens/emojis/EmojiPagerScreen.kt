package com.brainwallet.ui.screens.emojis
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.brainwallet.navigation.OnNavigate
import com.brainwallet.ui.screens.main.MainViewModel
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmojiPagerScreen(
    onNavigate: OnNavigate,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = koinViewModel(),
    onDismissPickEmojis: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    val hubPageCount = 2
    val pagerState = rememberPagerState(pageCount = { hubPageCount })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            pageSpacing = 8.dp,
            modifier = modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> HowToSetEmojisScreen(
                    onNextPage = {
                        scope.launch {
                            pagerState.animateScrollToPage(page + 1)
                        }
                    }
                )
                1 -> PickEmojisScreen(
                    firstThreeWords = viewModel.walletManager.getSeedWords(),
                    onEmojisPicked = { wereEmojisPicked ->
                        scope.launch {
                            if (wereEmojisPicked) {
                                onDismissPickEmojis()
                            } else {
                                onDismiss()
                            }
                        }
                    },
                )
            }
        }
    }
}
