package com.brainwallet.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import com.brainwallet.ui.theme.mainScreenLightSurfaceGradient
import androidx.compose.ui.unit.dp
import com.brainwallet.navigation.OnNavigate
import com.brainwallet.navigation.Route
import com.brainwallet.navigation.UiEffect
import com.brainwallet.ui.composable.BentoBottomNavBar
import com.brainwallet.ui.screens.settings.BentoRail
import com.brainwallet.ui.screens.settings.BentoSettingsButton
import com.brainwallet.ui.screens.settings.BentoThemeButton
import com.brainwallet.ui.screens.send.SendScreen
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brainwallet.constants.BWConstants.EXPAND_DURATION
import com.brainwallet.constants.BWConstants.FADE_IN_DURATION
import com.brainwallet.constants.BWConstants.FADE_OUT_DURATION
import com.brainwallet.data.model.AppSetting
import com.brainwallet.ui.bentosections.balancebento.BalanceBentoScreen
import com.brainwallet.ui.bentosections.gamehubbento.GameHubBentoScreen
import com.brainwallet.ui.bentosections.ltcpickerbento.LTCPickerBentoScreen
import com.brainwallet.ui.composable.HomeBentoContainer
import com.brainwallet.constants.balanceGameBentoHt
import com.brainwallet.constants.gameHubHt
import com.brainwallet.constants.statusBarPadding
import com.brainwallet.constants.transactionRowHt
import com.brainwallet.ui.bentosections.transactionbento.TransactionsBentoScreen
import com.brainwallet.ui.screens.buyreceive.BuyReceiveScreen
import com.brainwallet.ui.screens.gamehub.GameHubScreen
import com.brainwallet.ui.screens.main.MainScreenEvent
import com.brainwallet.ui.screens.main.MainViewModel
import com.brainwallet.ui.theme.BrainwalletAppTheme
import com.brainwallet.ui.theme.mainScreenDarkSurfaceGradient
import kotlinx.collections.immutable.toImmutableList

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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isSheetOpen by remember { mutableStateOf(false) }
    var modalContentRoute by remember { mutableStateOf<Route?>(null) }
    val appSetting by viewModel.appSetting.collectAsState()
    val isDarkMode = appSetting.isDarkMode
    val state by viewModel.state.collectAsStateWithLifecycle()
    val showTransactionDetail = state.showTransactionDetail
    val context = LocalContext.current
    val noTxItemsPresent = state.transactionItems.isEmpty()

    LaunchedEffect(Unit) {
        viewModel.onEvent(MainScreenEvent.OnLoad(context))
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                BentoRail(
                    appVersion = viewModel.versionLabel
                )
            }
        }
    ) {
        Scaffold(
            modifier = modifier.background(
                if (isDarkMode) mainScreenDarkSurfaceGradient else mainScreenLightSurfaceGradient
            ).padding(
                top = statusBarPadding
            ),
            containerColor = Color.Transparent,
            bottomBar = {
                BentoBottomNavBar(
                    isDarkMode = appSetting.isDarkMode,
                    currentRoute = currentRoute,
                    isShowingTransactionDetail = showTransactionDetail,
                    noTxItemsPresent = noTxItemsPresent,
                    onItemClick = { route: Route ->
                        currentRoute = route

                        if (route == Route.Main) {
                            scope.launch { sheetState.hide() }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    isSheetOpen = false
                                }
                            }
                            onNavigate.invoke(UiEffect.Navigate(route))
                        } else if (route == Route.History) {
                            viewModel.onEvent(MainScreenEvent.OnToggleTransactionsDetail)
                            if (showTransactionDetail) currentRoute = Route.Main
                        } else {
                            modalContentRoute = route
                            isSheetOpen = true
                        }
                    }
                )
            }

        ) { padding ->
            val gridItems = remember(state.transactionItems) {
                listOf(
                    "Tutorials Bento View",
                    "Favourites Bento View"
                )
            }
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
            ) {
                val verticalSpacing = 12.dp
                val topPadding = padding.calculateTopPadding()
                val bottomPadding = padding.calculateBottomPadding()
                val availableHeight = maxHeight -
                    (verticalSpacing * 5) -
                    topPadding -
                    balanceGameBentoHt -
                    transactionRowHt -
                    gameHubHt -
                    bottomPadding
                val transactionsDetailHeight = availableHeight + gameHubHt + balanceGameBentoHt
                val ltcPickerBentoHeight = (availableHeight * 0.78f) - (verticalSpacing / 2)
                val favoritesBentoHeight = (availableHeight * 0.22f) - (verticalSpacing / 2)
                val transactionBentoHeight by animateDpAsState(
                    targetValue = if (showTransactionDetail) transactionsDetailHeight else transactionRowHt,
                    animationSpec = tween(EXPAND_DURATION),
                    label = "transactionHeight"
                )

                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        BentoSettingsButton(
                            isDarkMode = isDarkMode,
                            onClick = {
                                scope.launch {
                                    drawerState.apply {
                                        if (isClosed) open() else close()
                                    }
                                }
                            }
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        BentoThemeButton(
                            isDarkMode = isDarkMode,
                            onClick = {
                                viewModel.onEvent(MainScreenEvent.OnToggleDarkMode)
                            }
                        )
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .padding(
                                horizontal = 12.dp,
                                vertical = 1.dp
                            )
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        userScrollEnabled = false
                    ) {
                        item(span = { GridItemSpan(2) }) {
                            Box(modifier = Modifier.height(balanceGameBentoHt)) {
                                BalanceBentoScreen(transactions = state.transactionItems.toImmutableList())
                            }
                        }
                        item(span = { GridItemSpan(2) }) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(transactionBentoHeight)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) {
                                        if (noTxItemsPresent) {
                                            onNavigate.invoke(UiEffect.Navigate(Route.MoonPayWeb))
                                        } else {
                                            viewModel.onEvent(MainScreenEvent.OnToggleTransactionsDetail)
                                        }
                                    }
                            ) {
                                TransactionsBentoScreen(
                                    transactions = state.transactionItems.toImmutableList(),
                                    toggleState = state.filterState,
                                    showTransactionDetail = state.showTransactionDetail,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        item(span = { GridItemSpan(1) }) {
                            AnimatedVisibility(
                                visible = !showTransactionDetail,
                                enter = fadeIn(tween(FADE_IN_DURATION)),
                                exit = fadeOut(tween(FADE_OUT_DURATION)),
                            ) {
                                HomeBentoContainer(
                                    name = gridItems[0],
                                    modifier = Modifier.height(availableHeight)
                                )
                            }
                        }
                        item(span = { GridItemSpan(1) }) {
                            AnimatedVisibility(
                                visible = !showTransactionDetail,
                                enter = fadeIn(tween(FADE_IN_DURATION)),
                                exit = fadeOut(tween(FADE_OUT_DURATION)),
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(modifier = Modifier.height(ltcPickerBentoHeight)) {
                                        LTCPickerBentoScreen()
                                    }
                                    HomeBentoContainer(
                                        name = gridItems[1],
                                        modifier = Modifier.height(favoritesBentoHeight)
                                    )
                                }
                            }
                        }
                        item(span = { GridItemSpan(2) }) {
                            AnimatedVisibility(
                                visible = !showTransactionDetail,
                                enter = fadeIn(tween(FADE_IN_DURATION)),
                                exit = fadeOut(tween(FADE_OUT_DURATION)),
                            ) {
                                Box(modifier = Modifier.height(gameHubHt)) {
                                    GameHubBentoScreen()
                                }
                            }
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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                ) {
                    when (modalContentRoute) {
                        Route.Send -> SendScreen(onNavigate = onNavigate)
                        Route.BuyReceive -> BuyReceiveScreen(onNavigate = onNavigate)
                        Route.GameHub -> GameHubScreen(onNavigate = onNavigate)
                        else -> {}
                    }
                }
            }
        }
    }
}

@Composable
@PreviewLightDark
fun MainScreenPreview() {
    BrainwalletAppTheme(appSetting = AppSetting(isDarkMode = isSystemInDarkTheme())) {
        MainScreen(onNavigate = {})
    }
}
