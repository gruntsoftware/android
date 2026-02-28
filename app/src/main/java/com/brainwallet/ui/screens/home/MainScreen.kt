package com.brainwallet.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.brainwallet.navigation.OnNavigate
import com.brainwallet.navigation.Route
import com.brainwallet.navigation.UiEffect
import com.brainwallet.ui.composable.BentoBottomNavBar
import com.brainwallet.ui.screens.settings.BentoRail
import com.brainwallet.ui.screens.settings.BentoSettingsButton
import com.brainwallet.ui.screens.settings.BentoThemeButton
import com.brainwallet.ui.screens.send.SendScreen
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ModalBottomSheet
import com.brainwallet.ui.bentosections.gamehubbento.GameHubBentoScreen
import com.brainwallet.ui.composable.HomeBentoContainer
import com.brainwallet.ui.screens.buyreceive.BuyReceiveScreen
import com.brainwallet.ui.screens.gamehub.GameHubScreen
import com.brainwallet.ui.screens.home.history.HistoryScreen

/**
 * The main screen of the application, featuring a bento-style grid layout.
 * It integrates the top bar, bottom navigation, and content grid.
 *
 * @param modifier The modifier to be applied to the component.
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigate: OnNavigate,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = koinViewModel()
) {
    var currentRoute by remember { mutableStateOf<Route>(Route.Main) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState()
    var isSheetOpen by remember { mutableStateOf(false) }
    var modalContentRoute by remember { mutableStateOf<Route?>(null) }

    val state by viewModel.state.collectAsState()
    val loadingState by viewModel.loadingState.collectAsState()
    val appSetting by viewModel.appSetting.collectAsState()
    val context = LocalContext.current

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
                        .padding(horizontal = 16.dp, vertical = 16.dp),
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
                        }
                    }
                }
            },
            bottomBar = {
                BentoBottomNavBar(
                    currentRoute = currentRoute,
                    onItemClick = { route: Route ->
                        currentRoute = route // Keep this to highlight the correct icon

                        if (route == Route.Main) {
                            // If Home is clicked, just ensure the sheet is closed
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    isSheetOpen = false
                                }
                            }
                        } else {
                            // For any other route, set the content and open the sheet
                            modalContentRoute = route
                            isSheetOpen = true
                        }
                        // The onNavigate call might still be needed depending on your navigation architecture
                        onNavigate.invoke(UiEffect.Navigate(route))
                    }
                )
            }

        ) { paddingValues ->
            val gridItems = remember {
                listOf(
                    "Balance Bento View",
                    "Transaction History View",
                    "Tutorials Bento View",
                    "LTC Price Bento View",
                    "Favourites Bento View"
                )
            }
            BoxWithConstraints(
                modifier = Modifier
                    .padding(paddingValues)
            ) {
                val balanceHeight = 105.dp
                val gameHubHeight = 120.dp
                val transRowHeight = 60.dp
                val availableHeight = this.maxHeight - balanceHeight - gameHubHeight - transRowHeight
                val tutorialsBentoHeight = availableHeight * 0.80f
                val bentoBox4Height = (tutorialsBentoHeight / 2) - 8.dp // Half of box 3, minus half the spacing
                val bentoBox5Height = (tutorialsBentoHeight / 2) - 8.dp // Half of box 3, minus half the spacing

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.padding(16.dp), // Apply your grid-specific padding here
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item(span = { GridItemSpan(2) }) {
                        HomeBentoContainer(name = gridItems[0], modifier = Modifier.height(balanceHeight))
                    }
                    item(span = { GridItemSpan(2) }) {
                        HomeBentoContainer(name = gridItems[1], modifier = Modifier.height(transRowHeight))
                    }
                    item(span = { GridItemSpan(1) }) {
                        HomeBentoContainer(name = gridItems[2], modifier = Modifier.height(tutorialsBentoHeight))
                    }
                    item(span = { GridItemSpan(1) }) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            HomeBentoContainer(name = gridItems[3], modifier = Modifier.height(bentoBox4Height))
                            HomeBentoContainer(name = gridItems[4], modifier = Modifier.height(bentoBox5Height))
                        }
                    }
                    item(span = { GridItemSpan(2) }) {
                        // GameHubBentoScreen already has its own internal height logic,
                        // so we just need to place it in a container with a defined height.
                        Box(modifier = Modifier.height(gameHubHeight)) {
                            GameHubBentoScreen()
                        }
                    }
                }
            }
        }

        if (isSheetOpen) {
            ModalBottomSheet(
                onDismissRequest = { isSheetOpen = false },
                sheetState = sheetState,
                dragHandle = null,
                shape = RoundedCornerShape(24.dp)
            ) {
                // Content of the bottom sheet
                when (modalContentRoute) {
                    Route.Send -> {
                        SendScreen(
                            onNavigate = onNavigate,
                            modifier = Modifier.padding(bottom = 32.dp)
                        )
                    }
                    Route.BuyReceive -> {
                        BuyReceiveScreen(onNavigate = onNavigate)
                    }
                    Route.GameHub -> {
                        GameHubScreen(
                            onNavigate = onNavigate,
                            modifier = Modifier.padding(bottom = 32.dp)
                        )
                    }
                    Route.History -> {
                        HistoryScreen(
                            onNavigate = onNavigate,
                            modifier = Modifier.padding(bottom = 32.dp)
                        )
                    }
                    else -> {
                        // Render nothing or a placeholder if the route is unexpected
                    }
                }
            }
        }
    }
}
