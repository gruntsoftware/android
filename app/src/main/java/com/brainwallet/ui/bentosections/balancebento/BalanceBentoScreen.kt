package com.brainwallet.ui.bentosections.balancebento

import android.R.attr.contentDescription
import android.R.attr.tint
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.R
import com.brainwallet.ui.screens.home.MainScreenEvent
import com.brainwallet.ui.screens.home.MainScreenState
import com.brainwallet.ui.screens.home.MainViewModel
import com.brainwallet.ui.theme.DesignTheme
import com.brainwallet.ui.theme.IBMPlexSans
import com.brainwallet.ui.theme.balanceBackgroundGradient
import com.brainwallet.ui.theme.bentoDarkBorderGradient
import com.brainwallet.ui.theme.bentoLightBorderGradient
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BalanceBentoScreen(
    modifier: Modifier = Modifier,
    viewModel: BalanceBentoViewModel = koinViewModel(),
    mainViewModel: MainViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val mainState by mainViewModel.state.collectAsState()

    BalanceBentoScreen(
        state = state,
        mainState = mainState,
        onEvent = viewModel::onEvent,
        onMainEvent = mainViewModel::onEvent,
        modifier = modifier
    )
}

@Composable
fun BalanceBentoScreen(
    state: BalanceBentoState,
    mainState: MainScreenState,
    onEvent: (BalanceBentoEvent) -> Unit,
    onMainEvent: (MainScreenEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var resizedLTCFiatFontSize by remember { mutableStateOf(44.sp) }
    var resizedAsOfFontSize by remember { mutableStateOf(10.sp) }
    var resizedLocalizedPriceFontSize by remember { mutableStateOf(20.sp) }
    val context = LocalContext.current

    val progressLabel = "%.2f".format(state.syncProgress * 100) + "%"
    val currentBlockLabel = stringResource(R.string.balance_bento_current_block_label) +
        " ${state.currentBlockHeight}"
    val lastBlockLabel = if (state.lastBlock == 0) {
        ""
    } else {
        stringResource(R.string.balance_bento_last_block_label) +
            "${state.lastBlock}"
    }
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
            Column {
                Text(
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
                    text = if (state.balanceHidden) "" else "Ł${state.ltcBalance}",
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 34.sp,
                        color = Color.White
                    ),
                    maxLines = 1
                )

                Text(
                    text = if (state.balanceHidden) "" else " ${state.symbol} ${state.fiatBalance}",
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.Light,
                        fontSize = 13.sp,
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
                        ) { onEvent(BalanceBentoEvent.OnToggleBalanceVisibility) },
                    painter = iconImage,
                    contentDescription = "Toggle Balance Show",
                    tint = Color.White
                )

                Text(
                    modifier = Modifier.align(Alignment.End)
                        .alpha(if (state.brainwalletIsSyncing) 1f else 0f),
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
                        .alpha(if (state.brainwalletIsSyncing) 1f else 0f),
                    text = lastBlockLabel,
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
                        .alpha(if (state.brainwalletIsSyncing) 1f else 0f),
                    text = if (lastBlockLabel == "") "" else state.lastTimeStamp,
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = Color.White
                    ),
                    maxLines = 1
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
                        modifier = Modifier.padding(end = 10.dp, bottom = 3.dp),
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
                        modifier = Modifier.padding(5.dp, bottom = 3.dp),
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

                    SyncStatusSubScreen()
                }
                LinearProgressIndicator(
                    progress = { state.syncProgress },
                    modifier = Modifier.fillMaxWidth()
                        .alpha(if (state.brainwalletIsSyncing) 1f else 0f),
                    color = DesignTheme.colors.affirm,
                    trackColor = Color.White.copy(0.5f),
                    strokeCap = StrokeCap.Round,
                    gapSize = (-15).dp,
                    drawStopIndicator = {}
                )
            }
        }
    }
}
