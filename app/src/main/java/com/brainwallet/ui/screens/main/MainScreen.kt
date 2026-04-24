package com.brainwallet.ui.screens.home

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brainwallet.constants.BWConstants
import com.brainwallet.constants.BWConstants.EXPAND_DURATION
import com.brainwallet.constants.BWConstants.FADE_IN_DURATION
import com.brainwallet.constants.BWConstants.FADE_OUT_DURATION
import com.brainwallet.constants.balanceGameBentoHt
import com.brainwallet.constants.bentoSpacer
import com.brainwallet.constants.gameHubHt
import com.brainwallet.constants.topNavButtonSize
import com.brainwallet.constants.topNavStartEndPadding
import com.brainwallet.constants.transactionRowHt
import com.brainwallet.data.model.AppSetting
import com.brainwallet.navigation.OnNavigate
import com.brainwallet.navigation.Route
import com.brainwallet.navigation.UiEffect
import com.brainwallet.tools.animation.BRAnimator
import com.brainwallet.tools.manager.AnalyticsManager
import com.brainwallet.ui.bentosections.balancebento.BalanceBentoScreen
import com.brainwallet.ui.bentosections.buyreceivebento.receive.ReceiveDialog
import com.brainwallet.ui.bentosections.favouritesbento.FavouritesBentoScreen
import com.brainwallet.ui.bentosections.gamehubbento.GameHubBentoPagerScreen
import com.brainwallet.ui.bentosections.ltcpickerbento.LTCPickerBentoScreen
import com.brainwallet.ui.bentosections.transactionbento.TransactionsBentoScreen
import com.brainwallet.ui.bentosections.tutorials.TutorialsBentoScreen
import com.brainwallet.ui.composable.BentoBottomNavBar
import com.brainwallet.ui.screens.main.MainScreenEvent
import com.brainwallet.ui.screens.main.MainViewModel
import com.brainwallet.ui.screens.main.SettingsButton
import com.brainwallet.ui.screens.main.ThemeButton
import com.brainwallet.ui.screens.send.SendScreen
import com.brainwallet.ui.screens.settings.settingsrows.HomeSettingDrawerSheet
import com.brainwallet.ui.theme.BrainwalletAppTheme
import com.brainwallet.ui.theme.bentoClearGradient
import com.brainwallet.ui.theme.bentoModalDarkGradient
import com.brainwallet.ui.theme.blurAnimatedWith
import com.brainwallet.ui.theme.mainScreenDarkSurfaceGradient
import com.brainwallet.ui.theme.mainScreenLightSurfaceGradient
import com.brainwallet.util.EventBus
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onNavigate: OnNavigate,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = koinViewModel()
) {
    val context = LocalContext.current
    var currentRoute by remember { mutableStateOf<Route>(Route.Main) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isSheetOpen by remember { mutableStateOf(false) }
    var modalContentRoute by remember { mutableStateOf<Route?>(null) }
    val appSetting by viewModel.appSetting.collectAsStateWithLifecycle()
    val isDarkMode = appSetting.isDarkMode
    val state by viewModel.state.collectAsStateWithLifecycle()
    val showTransactionDetail = state.showTransactionDetail
    val noTxItemsPresent = state.transactionItems.isEmpty()
    val blurRadiusWhen by animateFloatAsState(
        targetValue = if (isSheetOpen) 40f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "blurRadius"
    )

    val canUserSend = !state.brainwalletIsSyncing && state.isInternetReachable

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onEvent(MainScreenEvent.OnLoad(context))
        viewModel.onResume()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        viewModel.onPause()
    }

    LaunchedEffect(Unit) {
        EventBus.events
            .filter { it is EventBus.Event.Message }
            .map { it as EventBus.Event.Message }
            .collect {
                drawerState.close()
            }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = drawerState.isOpen,
        drawerContent = {
            HomeSettingDrawerSheet(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .padding(
                        end = topNavStartEndPadding +
                            topNavStartEndPadding +
                            topNavButtonSize
                    ),
            )
        }
    ) {
        Scaffold(
            modifier = modifier
                .background(
                    if (isDarkMode) {
                        mainScreenDarkSurfaceGradient
                    } else {
                        mainScreenLightSurfaceGradient
                    }
                )
                .blurAnimatedWith(blurRadiusWhen),
            containerColor = Color.Transparent,
            bottomBar = {
                BentoBottomNavBar(
                    isDarkMode = appSetting.isDarkMode,
                    currentRoute = currentRoute,
                    isShowingTransactionDetail = showTransactionDetail,
                    canUserSend = canUserSend,
                    noTxItemsPresent = noTxItemsPresent,
                    onToggleTransactionDetail = {
                        viewModel.onEvent(MainScreenEvent.OnToggleTransactionsDetail)
                        if (showTransactionDetail) currentRoute = Route.Main
                    },
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
                    },
                )
            }

        ) { padding ->
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                val topNavRowHt = topNavButtonSize + (topNavStartEndPadding * 2)
                val availableHeight = maxHeight -
                    topNavRowHt -
                    (bentoSpacer * 5) -
                    balanceGameBentoHt -
                    transactionRowHt -
                    gameHubHt
                val transactionsDetailHeight = availableHeight + gameHubHt + balanceGameBentoHt
                val ltcPickerBentoHeight = (availableHeight * 0.7f) - (bentoSpacer / 2)
                val favoritesBentoHeight = (availableHeight * 0.3f) - (bentoSpacer / 2)
                val transactionBentoHeight by animateDpAsState(
                    targetValue = if (showTransactionDetail) transactionsDetailHeight else transactionRowHt,
                    animationSpec = tween(EXPAND_DURATION),
                    label = "transactionHeight"
                )
                Timber.d(
                    "bento layout maxHeight=$maxHeight "
                )
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = topNavStartEndPadding,
                                vertical = topNavStartEndPadding
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ThemeButton(
                            isDarkMode = isDarkMode,
                            onClick = {
                                viewModel.onEvent(MainScreenEvent.OnToggleDarkMode)
                            }
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        SettingsButton(
                            isDarkMode = isDarkMode,
                            onClick = {
                                scope.launch {
                                    drawerState.apply {
                                        if (isClosed) open() else close()
                                    }
                                }
                            }
                        )
                    }
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .padding(
                                horizontal = bentoSpacer,
                                vertical = 1.dp
                            )
                            .weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(bentoSpacer),
                        verticalArrangement = Arrangement.spacedBy(bentoSpacer),
                        userScrollEnabled = false
                    ) {
                        item(span = { GridItemSpan(2) }) {
                            Box(modifier = Modifier.height(balanceGameBentoHt)) {
                                BalanceBentoScreen(transactions = state.transactionItems.toImmutableList())
                            }
                        }
                        item(span = { GridItemSpan(2) }) {
                            TransactionsBentoScreen(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(transactionBentoHeight),
                                transactions = state.transactionItems.toImmutableList(),
                                toggleState = state.filterState,
                                showTransactionDetail = state.showTransactionDetail,
                                shouldShowFiatValues = state.shouldShowFiatValues,
                                onBentoTap = {
                                    if (noTxItemsPresent) {
                                        onNavigate.invoke(UiEffect.Navigate(Route.MoonPayWeb))
                                    } else {
                                        viewModel.onEvent(MainScreenEvent.OnToggleTransactionsDetail)
                                    }
                                }
                            )
                        }
                        item(span = { GridItemSpan(1) }) {
                            AnimatedVisibility(
                                visible = !showTransactionDetail,
                                enter = fadeIn(tween(FADE_IN_DURATION)),
                                exit = fadeOut(tween(FADE_OUT_DURATION)),
                            ) {
                                Box(modifier = Modifier.height(availableHeight)) {
                                    TutorialsBentoScreen()
                                }
                            }
                        }
                        item(span = { GridItemSpan(1) }) {
                            AnimatedVisibility(
                                visible = !showTransactionDetail,
                                enter = fadeIn(tween(FADE_IN_DURATION)),
                                exit = fadeOut(tween(FADE_OUT_DURATION)),
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(bentoSpacer)) {
                                    Box(modifier = Modifier.height(ltcPickerBentoHeight)) {
                                        LTCPickerBentoScreen()
                                    }
                                    Box(modifier = Modifier.height(favoritesBentoHeight)) {
                                        FavouritesBentoScreen()
                                    }
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
                                    GameHubBentoPagerScreen(onClick = { page ->
                                        when (page) {
                                            0 -> {
                                                AnalyticsManager.logCustomEvent("user_did_tap_fallinmoji_no_op")
                                            }
                                            1 -> {
                                                modalContentRoute = Route.BuyReceive
                                                isSheetOpen = true
                                                AnalyticsManager.logCustomEvent("user_did_tap_mp_in_gh")
                                            }
                                            2 -> {
                                                val builder = CustomTabsIntent.Builder()
                                                val customTabsIntent = builder.build()
                                                customTabsIntent.launchUrl(
                                                    context,
                                                    Uri.parse(BWConstants.LINKTREE_URL)
                                                )

                                                AnalyticsManager.logCustomEvent("user_did_tap_linktree")
                                            }
                                        }
                                    })
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isSheetOpen) {
            ModalBottomSheet(
                modifier = Modifier
                    .imePadding(),
                onDismissRequest = { isSheetOpen = false },
                sheetState = sheetState,
                dragHandle = { BottomSheetDefaults.DragHandle() },
                containerColor = Color.Transparent,
                contentWindowInsets = { WindowInsets(0) },
                scrimColor = if (isDarkMode) {
                    Color.White.copy(0.1f)
                } else {
                    Color.Black.copy(0.1f)
                },
                shape = RoundedCornerShape(24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentWidth()
                        .fillMaxHeight(0.85f)
                        .background(
                            brush = if (modalContentRoute == Route.BuyReceive) {
                                bentoClearGradient
                            } else if (isDarkMode && modalContentRoute == Route.Send) {
                                bentoModalDarkGradient
                            } else {
                                mainScreenLightSurfaceGradient
                            },
                            shape = RoundedCornerShape(24.dp)
                        )

                ) {
                    when (modalContentRoute) {
                        Route.Send -> SendScreen(
                            onNavigate = onNavigate,
                            onOpenScanner = {
                                val activity = context as? FragmentActivity
                                activity?.let {
                                    BRAnimator.openScanner(
                                        it,
                                        BWConstants.SCANNER_REQUEST
                                    )
                                }
                            },
                            onDimissSendModal = { isSheetOpen = false }
                        )

                        Route.BuyReceive -> ReceiveDialog(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .align(Alignment.Center),
                            onDismissRequest = { isSheetOpen = false }
                        )
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
