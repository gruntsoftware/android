package com.brainwallet.ui.bentosections.balancebento

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.brainwallet.R
import com.brainwallet.ui.screens.main.MainScreenEvent
import com.brainwallet.ui.screens.main.MainScreenState
import com.brainwallet.ui.screens.main.MainViewModel
import com.brainwallet.ui.theme.DesignTheme
import com.brainwallet.ui.theme.IBMPlexSans
import com.brainwallet.ui.theme.balanceBackgroundGradient
import com.brainwallet.ui.theme.bentoDarkBorderGradient
import com.brainwallet.ui.theme.bentoLightBorderGradient
import com.brainwallet.ui.theme.blurWhen
import org.koin.android.compat.ScopeCompat.viewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BalanceBentoScreen(
    modifier: Modifier = Modifier,
    viewModel: BalanceBentoViewModel = koinViewModel(),
    mainViewModel: MainViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val mainState by mainViewModel.state.collectAsState()
    val context = LocalContext.current

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onResume()
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onPause()
    }

    BalanceBentoScreen(
        state = state,
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
    mainState: MainScreenState,
    onEvent: (BalanceBentoEvent) -> Unit,
    onMainEvent: (MainScreenEvent) -> Unit,
    modifier: Modifier = Modifier,
    onDebugStatusUpdate: () -> Unit = {},
    onDebugTxAdded: () -> Unit = {},
    onDebugBalanceChanged: () -> Unit = {},
) {
    val infiniteTransition = rememberInfiniteTransition()
    val throbOpacity by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse)
    )

    // ── swap animation ────────────────────────────────────────────────────
    var isSwapped by remember { mutableStateOf(false) }
    val primarySize by animateFloatAsState(
        targetValue = if (isSwapped) 13f else 32f,
        animationSpec = tween(300),
        label = "primarySize"
    )
    val secondarySize by animateFloatAsState(
        targetValue = if (isSwapped) 32f else 13f,
        animationSpec = tween(300),
        label = "secondarySize"
    )
    val primaryWeight = if (isSwapped) FontWeight.Light else FontWeight.Bold
    val secondaryWeight = if (isSwapped) FontWeight.Bold else FontWeight.Light

    val primaryVerticalOffset = if (isSwapped) 0.dp else -2.dp
    val secondaryVerticalOffset = if (isSwapped) -2.dp else 0.dp

    val progressLabel = "%.2f".format(state.syncProgress * 100) + "%"
    val currentBlockLabel = stringResource(R.string.balance_bento_current_block_label) +
        " ${state.currentBlockHeight}"
    val currentTxsLabel = stringResource(R.string.current_transaction_count) + " %d".format(state.transactions.size)
    val iconImage: Painter

    if (state.balanceHidden) {
        iconImage = painterResource(id = R.drawable.visibility_svg)
    } else {
        iconImage = painterResource(id = R.drawable.visibility_off)
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
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { isSwapped = !isSwapped }
            ) {
                Text(
                    modifier = Modifier
                        .blurWhen(!state.isInternetReachable),
                    text = stringResource(R.string.my_balance),
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = Color.White
                    ),
                    maxLines = 1
                )
                Text(
                    modifier = Modifier.offset(y = primaryVerticalOffset)
                        .align(Alignment.Start)
                        .blurWhen(!state.isInternetReachable),
                    text = if (state.balanceHidden) "" else "Ł${state.litoshiBalance}",
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = primaryWeight,
                        fontSize = primarySize.sp,
                        color = Color.White
                    ),
                    maxLines = 1
                )
                Text(
                    modifier = Modifier.offset(y = secondaryVerticalOffset)
                        .align(Alignment.Start)
                        .blurWhen(!state.isInternetReachable),
                    text = if (state.balanceHidden) "" else " ${state.symbol} ${state.fiatBalanceFormatted}",
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = secondaryWeight,
                        fontSize = secondarySize.sp,
                        color = Color.White
                    ),
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.weight(0.5f))

            Column {
                Icon(
                    modifier = Modifier.align(Alignment.End)
                        .size(30.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                        ) { onEvent(BalanceBentoEvent.OnToggleBalanceVisibility) }
                        .blurWhen(!state.isInternetReachable),
                    painter = iconImage,
                    contentDescription = "Toggle Balance Show",
                    tint = Color.White
                )
                Text(
                    modifier = Modifier.align(Alignment.End)
                        .alpha(if (state.brainwalletIsSyncing) 1f else 0f)
                        .blurWhen(!state.isInternetReachable),
                    text = state.topMessage,
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = Color.White
                    ),
                    maxLines = 1
                )
                Text(
                    modifier = Modifier.align(Alignment.End)
                        .alpha(if (state.brainwalletIsSyncing) 1f else 0f)
                        .blurWhen(!state.isInternetReachable),
                    text = currentTxsLabel,
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = Color.White
                    ),
                    maxLines = 1
                )
                Text(
                    modifier = Modifier.align(Alignment.End)
                        .alpha(if (state.brainwalletIsSyncing) 1f else 0f)
                        .blurWhen(!state.isInternetReachable),
                    text = state.lastTimeStamp,
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                )
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
                            .blurWhen(!state.isInternetReachable)
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
                            .blurWhen(!state.isInternetReachable),
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
                        isInternetReachable = !state.isInternetReachable
                    )
                }
                LinearProgressIndicator(
                    progress = { state.syncProgress },
                    modifier = Modifier.fillMaxWidth()
                        .alpha(if (state.brainwalletIsSyncing) 1f else 0f)
                        .blurWhen(!state.isInternetReachable),
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
            isInternetReachable = !state.isInternetReachable
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
