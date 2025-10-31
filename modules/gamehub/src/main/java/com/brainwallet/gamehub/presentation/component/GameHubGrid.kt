package com.brainwallet.gamehub.presentation.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.brainwallet.design.component.effect.CardOpacityContainer
import com.brainwallet.design.component.widget.GridChip
import com.brainwallet.design.component.widget.PaginationDot
import com.brainwallet.gamehub.R
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

data class GamehubData(
    val banner: Int
) {
    companion object {
        val default = persistentListOf(
            GamehubData(R.drawable.bg_game_hub_fallingmoji)
        )
    }
}

@Composable
fun GameHubGrid(
    modifier: Modifier = Modifier,
    games: PersistentList<GamehubData> = GamehubData.default,
) {
    val pagerState = rememberPagerState(pageCount = { games.size })

    CardOpacityContainer(
        modifier = modifier.fillMaxWidth()
    ) {
        Box(
            Modifier.fillMaxSize()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.matchParentSize()
            ) { page ->
                val currentGames = games[page]
                Image(
                    painter = painterResource(currentGames.banner),
                    contentDescription = "Game Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                games.forEachIndexed { index, _ ->
                    PaginationDot(
                        isActive = index == pagerState.currentPage,
                        modifier = Modifier.padding(horizontal = 3.dp)
                    )
                }
            }
            GridChip(
                "Game Hub",
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
            )
        }
    }
}

@PreviewLightDark
@Preview
@Composable
private fun GameHubGridPreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        GameHubGrid()
    }
}
