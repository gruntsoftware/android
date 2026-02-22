package com.brainwallet.ui.screens.home

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.brainwallet.ui.composable.BentoBottomNavBar
import com.brainwallet.ui.screens.settings.BentoRail
import com.brainwallet.ui.screens.settings.BentoSettingsButton
import com.brainwallet.ui.screens.settings.BentoThemeButton
import com.brainwallet.ui.composable.HomeBentoContainer
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme
import kotlinx.coroutines.launch

/**
 * The main screen of the application, featuring a bento-style grid layout.
 * It integrates the top bar, bottom navigation, and content grid.
 *
 * @param modifier The modifier to be applied to the component.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier
) {
    var currentRoute by remember { mutableStateOf("send") }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                BentoRail(
                    appVersion = "v.X.X.X (XXXXXXXXXXXX)"
                )
            }
        }
    ) {
        Scaffold(
            modifier = modifier,
            containerColor = BrainwalletTheme.colors.surface,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BentoSettingsButton {
                        scope.launch {
                            drawerState.apply {
                                if (isClosed) open() else close()
                            }
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))

                    BentoThemeButton {
                        scope.launch {
                            print("Theme button clicked")
// In your legacy Activity/Fragment
//                            viewLifecycleOwner.lifecycleScope.launch {
//                                EventBus.events.collect { event ->
//                                    when (event) {
//                                        is EventBus.Event.Message -> {
//                                            if (event.message == SettingsViewModel.LEGACY_EFFECT_ON_TOGGLE_DARK_MODE) {
//                                                // React to dark mode toggle if needed
//                                                // The theme will already be updated via Compose
//                                            }
//                                        }
//                                    }
//                                }
//                            }
                        }
                    }
                }
            },
            bottomBar = {
                BentoBottomNavBar(currentRoute = currentRoute, onItemClick = { currentRoute = it })
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
                    HomeBentoContainer(name = gridItems[0], modifier = Modifier.height(150.dp))
                }
                item(span = { GridItemSpan(2) }) {
                    HomeBentoContainer(name = gridItems[1], modifier = Modifier.height(100.dp))
                }
                item(span = { GridItemSpan(1) }) {
                    HomeBentoContainer(name = gridItems[2], modifier = Modifier.height(220.dp))
                }
                item(span = { GridItemSpan(1) }) {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        HomeBentoContainer(name = gridItems[3], modifier = Modifier.height(100.dp))
                        HomeBentoContainer(name = gridItems[4], modifier = Modifier.height(100.dp))
                    }
                }
                item(span = { GridItemSpan(2) }) {
                    HomeBentoContainer(name = gridItems[5], modifier = Modifier.height(120.dp))
                }
            }
        }
    }
}

@Composable
@PreviewLightDark
fun MainScreenPreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        MainScreen()
    }
}
