package com.brainwallet.ui.bentosections.balancebento

import android.media.MediaPlayer
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brainwallet.R
import com.brainwallet.presenter.entities.TxItem
import com.brainwallet.ui.screens.main.MainScreenEvent
import com.brainwallet.ui.screens.main.MainScreenState
import com.brainwallet.ui.screens.main.MainViewModel
import com.brainwallet.ui.theme.DesignTheme
import com.brainwallet.ui.theme.IBMPlexSans
import com.brainwallet.ui.theme.balanceBackgroundGradient
import com.brainwallet.ui.theme.bentoDarkBorderGradient
import com.brainwallet.ui.theme.bentoLightBorderGradient
import com.brainwallet.ui.theme.blurWhen
import kotlinx.collections.immutable.ImmutableList
import org.koin.compose.viewmodel.koinViewModel
import java.math.BigDecimal

@Composable
fun BalanceBentoScreen(
    transactions: ImmutableList<TxItem>,
    modifier: Modifier = Modifier,
    viewModel: BalanceBentoViewModel = koinViewModel(),
    mainViewModel: MainViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val mainState by mainViewModel.state.collectAsState()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onEvent(BalanceBentoEvent.OnLoad)
        viewModel.onResume()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        viewModel.onPause()
    }

    BalanceBentoScreen(
        state = state,
        transactions = transactions,
        mainState = mainState,
        onEvent = viewModel::onEvent,
        onMainEvent = mainViewModel::onEvent,
        modifier = modifier,
        onDebugStatusUpdate = viewModel::debugTriggerStatusUpdate,
        onDebugTxAdded = viewModel::debugTriggerTxAdded,
        onDebugBalanceChanged = viewModel::debugTriggerBalanceChanged,
    )
}

@Composable
fun BalanceBentoScreen(
    state: BalanceBentoState,
    transactions: ImmutableList<TxItem>,
    mainState: MainScreenState,
    onEvent: (BalanceBentoEvent) -> Unit,
    onMainEvent: (MainScreenEvent) -> Unit,
    modifier: Modifier = Modifier,
    onDebugStatusUpdate: () -> Unit = {},
    onDebugTxAdded: () -> Unit = {},
    onDebugBalanceChanged: () -> Unit = {},
) {
    val context = LocalContext.current
    val infiniteTransition = rememberInfiniteTransition()
    var previousBalance by remember { mutableStateOf(BigDecimal.ZERO) }

    val throbOpacity by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse)
    )

    // ── swap animation ────────────────────────────────────────────────────
    var isSwapped by remember { mutableStateOf(false) }
    val primaryOffset by animateDpAsState(
        targetValue = if (isSwapped) 40.dp else 0.dp,
        animationSpec = spring(
            dampingRatio = 0.5f,
            stiffness = Spring.StiffnessVeryLow,
        ),
        label = "primaryOffset",
    )
    val secondaryOffset by animateDpAsState(
        targetValue = if (isSwapped) 0.dp else 40.dp,
        animationSpec = spring(
            dampingRatio = 0.5f,
            stiffness = Spring.StiffnessVeryLow,
        ),
        label = "secondaryOffset",
    )

    val progressLabel = "%.2f".format(state.syncProgress * 100) + "%"
    val currentBlockLabel = stringResource(R.string.block_height_label) +
        " ${state.currentBlockHeight}"
    val currentTxsLabel =
        stringResource(R.string.current_transaction_count) + " %d".format(transactions.size)
    val iconImage: Painter

    if (state.balanceHidden) {
        iconImage = painterResource(id = R.drawable.visibility_svg)
    } else {
        iconImage = painterResource(id = R.drawable.visibility_off)
    }
    val coinAudioPlayer = remember { MediaPlayer.create(context, R.raw.coinflip) }
    val previousCount = remember { mutableIntStateOf(state.transactions.size) }

    // Listen for changes in number of transactions
    LaunchedEffect(state.transactions) {
        if (state.transactions.size > previousCount.intValue) {
            coinAudioPlayer.start()
        }
        previousCount.intValue = state.transactions.size
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(
                brush = balanceBackgroundGradient,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.5.dp,
                brush = if (state.darkMode) bentoDarkBorderGradient else bentoLightBorderGradient,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 28.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { isSwapped = !isSwapped }
            ) {
                // ──────── MY BALANCE LABEL ────────
                Text(
                    modifier = Modifier
                        .blurWhen(!mainState.isInternetReachable),
                    text = stringResource(R.string.my_balance),
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = Color.White
                    ),
                    maxLines = 1
                )
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.5f)
                ) {
                    // ──────── TOP CURRENCY ────────
                    Text(
                        text = if (state.balanceHidden) "" else "Ł${mainState.litoshiBalance}",
                        style = TextStyle(
                            fontFamily = IBMPlexSans,
                            fontWeight = if (isSwapped) FontWeight.Light else FontWeight.SemiBold,
                            fontSize = if (isSwapped) 13.sp else 30.sp,
                            color = Color.White,
                        ),
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .blurWhen(!mainState.isInternetReachable)
                            .offset(y = primaryOffset)
                            .zIndex(if (isSwapped) 1f else 0f)

                    )
                    // ──────── BOTTOM CURRENCY ────────
                    Text(
                        text = if (state.balanceHidden) {
                            ""
                        } else {
                            "${mainState.selectedCurrency.symbol}${mainState.fiatBalanceFormatted}"
                        },
                        style = TextStyle(
                            fontFamily = IBMPlexSans,
                            fontWeight = if (isSwapped) FontWeight.SemiBold else FontWeight.Light,
                            fontSize = if (isSwapped) 30.sp else 13.sp,
                            color = Color.White,
                        ),
                        maxLines = 1,
                        modifier = Modifier
                            .fillMaxWidth()
                            .blurWhen(!mainState.isInternetReachable)
                            .offset(y = secondaryOffset)
                            .zIndex(if (isSwapped) 0f else 1f)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.weight(1f))
            Column(
                modifier = Modifier
                    .fillMaxHeight()
            ) {
                Icon(
                    modifier = Modifier.align(Alignment.End)
                        .padding(bottom = 5.dp)
                        .size(30.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                        ) { onEvent(BalanceBentoEvent.OnToggleBalanceVisibility) }
                        .blurWhen(!mainState.isInternetReachable),
                    painter = iconImage,
                    contentDescription = "Toggle Balance Show",
                    tint = Color.White
                )
                // ──────── SYNC MESSAGE ────────
                Text(
                    modifier = Modifier.align(Alignment.End)
                        .alpha(if (state.brainwalletIsSyncing) 1f else 0f)
                        .blurWhen(!mainState.isInternetReachable),
                    text = state.topMessage,
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = Color.White
                    ),
                    maxLines = 1
                )
                // ──────── CURRENT TXNS LABEL ────────
                Text(
                    modifier = Modifier.align(Alignment.End)
                        .alpha(if (state.brainwalletIsSyncing) 1f else 0f)
                        .blurWhen(!mainState.isInternetReachable),
                    text = currentTxsLabel,
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = Color.White
                    ),
                    maxLines = 1
                )
                // ──────── TIMESTAMP LABEL ────────
                Text(
                    modifier = Modifier.align(Alignment.End)
                        .alpha(if (state.brainwalletIsSyncing) 1f else 0f)
                        .blurWhen(!mainState.isInternetReachable),
                    text = state.lastTimeStamp,
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Spacer(
                    modifier = Modifier.weight(0.5f)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(if (state.brainwalletIsSyncing) 1f else 0f),
                    verticalAlignment = Alignment.CenterVertically,

                ) {
                    Text(
                        modifier = Modifier
                            .width(70.dp)
                            .padding(bottom = 3.dp)
                            .blurWhen(!mainState.isInternetReachable)
                            .graphicsLayer {
                                alpha = throbOpacity
                            },
                        text = progressLabel,
                        style = TextStyle(
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color.White
                        ),
                        maxLines = 1
                    )

                    Text(
                        modifier = Modifier.padding(2.dp, bottom = 3.dp)
                            .blurWhen(!mainState.isInternetReachable),
                        text = if (state.currentBlockHeight > 1) currentBlockLabel else "",
                        style = TextStyle(
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.Normal,
                            fontSize = 13.sp,
                            color = Color.White
                        ),
                        maxLines = 1
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    SyncStatusSubScreen(
                        modifier = Modifier,
                        isInternetReachable = !mainState.isInternetReachable
                    )
                }
                LinearProgressIndicator(
                    progress = { state.syncProgress },
                    modifier = Modifier.fillMaxWidth()
                        .alpha(if (state.brainwalletIsSyncing) 1f else 0f)
                        .blurWhen(!mainState.isInternetReachable),
                    color = DesignTheme.colors.affirm,
                    trackColor = Color.White.copy(0.5f),
                    strokeCap = StrokeCap.Round,
                    gapSize = (-15).dp,
                    drawStopIndicator = {}
                )
            }
        }
        NoWifiBalanceAlertScreen(
            modifier = Modifier,
            isInternetReachable = !mainState.isInternetReachable
        )
//  Uncomment to trigger BRWalletManager Callbacks
//        if (BuildConfig.DEBUG) {
//            Row(modifier = Modifier.background(Color.Red)) {
//                Button(onClick = onDebugStatusUpdate) { Text("Status Update") }
//                Button(onClick = onDebugTxAdded) { Text("Tx Added") }
//                Button(onClick = onDebugBalanceChanged) { Text("Balance Changed") }
//            }
//        }
    }
}
