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
import com.brainwallet.design.component.effect.AnimatedLightBleedBackground
import com.brainwallet.design.component.effect.DrawerGlassContainer
import com.brainwallet.design.component.effect.LightGlassContainer
import com.brainwallet.design.component.rail.BentoRail
import com.brainwallet.design.component.widget.BentoBottomNavBar
import com.brainwallet.design.component.widget.BentoHomeGrid
import com.brainwallet.ltc.presentation.component.BalanceBentoGrid
import com.brainwallet.design.component.widget.BentoRailButton
import com.brainwallet.design.component.widget.BentoTopBarActions
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
                        BentoRailButton {
                            isRailOpen = !isRailOpen
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        BentoTopBarActions(onSettingsClick = {}, onNotificationsClick = {})
                    }
                },
                bottomBar = {
                    LightGlassContainer(
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
                val gridItems = remember {
                    listOf(
                        "Balance Bento View",
                        "Transaction History View",
                        "Tutorials Bento View",
                        "LTC Price Bento View",
                        "Favourites Bento View",
                        "Game Hub Bento View"
                    )
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .padding(paddingValues)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item(span = { GridItemSpan(2) }) {
                        BalanceBentoGrid(modifier = Modifier.height(150.dp))
                    }
                    item(span = { GridItemSpan(2) }) {
                        BentoHomeGrid(name = gridItems[1], modifier = Modifier.height(100.dp))
                    }
                    item(span = { GridItemSpan(1) }) {
                        BentoHomeGrid(name = gridItems[2], modifier = Modifier.height(220.dp))
                    }
                    item(span = { GridItemSpan(1) }) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            BentoHomeGrid(name = gridItems[3], modifier = Modifier.height(100.dp))
                            BentoHomeGrid(name = gridItems[4], modifier = Modifier.height(100.dp))
                        }
                    }
                    item(span = { GridItemSpan(2) }) {
                        BentoHomeGrid(name = gridItems[5], modifier = Modifier.height(120.dp))
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
            DrawerGlassContainer(
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
    BrainwalletTheme(isSystemInDarkTheme()) {
        BentoMainScreen()
    }
}
