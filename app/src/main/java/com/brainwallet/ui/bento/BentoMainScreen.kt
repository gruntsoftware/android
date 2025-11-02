package com.brainwallet.ui.bento

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.brainwallet.design.presentation.component.effect.AnimatedLightBleedBackground
import com.brainwallet.design.presentation.component.effect.DrawerOpacityContainer
import com.brainwallet.design.presentation.component.effect.LightOpacityContainer
import com.brainwallet.design.presentation.component.rail.BentoRail
import com.brainwallet.design.presentation.component.widget.BentoBottomNavBar
import com.brainwallet.ltc.presentation.component.BalanceBentoGrid
import com.brainwallet.ltc.presentation.component.FavoriteGrid
import com.brainwallet.ltc.presentation.component.PriceTickerGrid
import com.brainwallet.ltc.presentation.component.TransactionHistoryGrid
import com.brainwallet.design.presentation.component.widget.BentoRailButton
import com.brainwallet.design.presentation.component.widget.BentoDarkModeToggle
import com.brainwallet.design.presentation.state.DarkModeState
import com.brainwallet.design.presentation.state.rememberDarkModeState
import com.brainwallet.gamehub.presentation.component.GameHubGrid
import com.brainwallet.tutorial.presentation.component.TutorialGrid
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

/**
 * Main application screen featuring a push-to-side rail navigation with smooth animations.
 *
 * The screen consists of:
 * - Animated light bleed background
 * - Main content area with bento grid layout
 * - Slide-out rail navigation menu
 * - Glass effect containers for premium visual appeal
 *
 * @param modifier Optional modifier for the root container
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BentoMainScreen(
    modifier: Modifier = Modifier
) {
    val darkModeState = rememberDarkModeState()
    BrainwalletTheme(darkModeState.isDarkMode) {
        BentoMainScreen(darkModeState, modifier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BentoMainScreen(
    darkModeState: DarkModeState,
    modifier: Modifier = Modifier
) {
    var currentRoute by remember { mutableStateOf("send") }
    var isRailOpen by remember { mutableStateOf(false) }

    val railWidth = 300.dp
    val premiumEasing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1.0f)

    val mainContentOffset by animateDpAsState(
        targetValue = if (isRailOpen) railWidth + 16.dp else 0.dp,
        animationSpec = tween(
            durationMillis = 500,
            easing = premiumEasing
        ),
        label = "mainContentOffset"
    )

    val railOffset by animateDpAsState(
        targetValue = if (isRailOpen) 0.dp else -railWidth,
        animationSpec = tween(
            durationMillis = 450,
            easing = premiumEasing
        ),
        label = "railOffset"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AnimatedLightBleedBackground(
            modifier = Modifier.fillMaxSize(),
            animationDurationMs = 12000,
            bleedIntensity = 0.12f
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = mainContentOffset)
                .clip(
                    if (isRailOpen) {
                        RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
                    } else {
                        RoundedCornerShape(0.dp)
                    }
                )
                .zIndex(1f)
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                topBar = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BentoDarkModeToggle(darkModeState = darkModeState)
                        Spacer(modifier = Modifier.weight(1f))
                        BentoRailButton {
                            isRailOpen = !isRailOpen
                        }
                    }
                },
                bottomBar = {
                    LightOpacityContainer(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    ) {
                        BentoBottomNavBar(
                            currentRoute = currentRoute,
                            onItemClick = { currentRoute = it }
                        )
                    }
                }
            ) { paddingValues ->
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item(span = { GridItemSpan(2) }) {
                        BalanceBentoGrid(modifier = Modifier.height(160.dp))
                    }
                    item(span = { GridItemSpan(2) }) {
                        TransactionHistoryGrid(modifier = Modifier.height(100.dp))
                    }
                    item(span = { GridItemSpan(1) }) {
                        TutorialGrid(modifier = Modifier.height(260.dp))
                    }
                    item(span = { GridItemSpan(1) }) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            PriceTickerGrid(modifier = Modifier.height(122.dp))
                            FavoriteGrid(modifier = Modifier.height(122.dp))
                        }
                    }
                    item(span = { GridItemSpan(2) }) {
                        GameHubGrid(modifier = Modifier.height(120.dp))
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(railWidth)
                .offset(x = railOffset)
                .zIndex(2f)
        ) {
            DrawerOpacityContainer(
                modifier = Modifier.fillMaxSize()
            ) {
                BentoRail(
                    userName = "Joseph Sanjaya",
                    appVersion = "v.X.X.X (XXXXXXXXXXXX)"
                )
            }
        }

        if (isRailOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = railWidth)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        isRailOpen = false
                    }
                    .zIndex(3f)
            )
        }
    }
}

@PreviewLightDark
@Composable
fun BentoMainScreenPreview() {
    val darkModeState = DarkModeState(isSystemInDarkTheme())
    BrainwalletTheme(darkModeState.isDarkMode) {
        BentoMainScreen(darkModeState)
    }
}
