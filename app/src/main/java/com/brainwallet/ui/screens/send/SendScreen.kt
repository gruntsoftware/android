package com.brainwallet.ui.screens.send

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.R
import com.brainwallet.navigation.OnNavigate
import com.brainwallet.navigation.UiEffect
import com.brainwallet.ui.composable.SendContinueButton
import com.brainwallet.ui.screens.main.MainViewModel
import org.koin.compose.viewmodel.koinViewModel
import com.brainwallet.ui.theme.IBMPlexSans
import kotlinx.coroutines.launch

@Composable
fun SendScreen(
    onNavigate: OnNavigate,
    modifier: Modifier = Modifier,
    viewModel: SendViewModel = koinViewModel(),
    mainViewModel: MainViewModel = koinViewModel()
) {
    val uiState by viewModel.state.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.onEvent(SendEvent.OnLoad)
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is UiEffect.Navigate -> onNavigate.invoke(effect)
                else -> Unit
            }
        }
    }
    SendScreen(uiState = uiState, modifier = modifier, onEvent = viewModel::onEvent)
}

@Composable
private fun SendScreen(
    uiState: SendState,
    modifier: Modifier = Modifier,
    onEvent: (SendEvent) -> Unit = {},
    viewModel: SendViewModel = koinViewModel(),
    mainViewModel: MainViewModel = koinViewModel()
) {
    val horizontalVerticalSpacing = 8
    val state by viewModel.state.collectAsState()
    val mainState by mainViewModel.state.collectAsState()
    val loadingState by viewModel.loadingState.collectAsState()
    val context = LocalContext.current
    val bottomButtonPadding = 60.dp
    var sendDataIsReady by remember { mutableStateOf(false) }

    val pageCount = 2
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                modifier = Modifier.padding(24.dp),
                text = if (pagerState.targetPage == 0) {
                    stringResource(R.string.send_litecoin_label)
                } else {
                    stringResource(
                        R.string.confirm_send_label
                    )
                },
                style = TextStyle(
                    fontFamily = IBMPlexSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                    color = if (state.darkMode) Color.White else Color.Black
                ),
                maxLines = 1
            )

            HorizontalPager(
                state = pagerState,
                Modifier.fillMaxHeight(0.65f)
            ) { page ->
                if (page == 0) {
                    PreSend(
                        modifier = Modifier.fillMaxHeight(0.65f)
                    )
                } else {
                    ConfirmSend(modifier = Modifier.fillMaxHeight(0.65f))
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Row {
                Text(
                    modifier = Modifier.padding(
                        start = 14.dp
                    ),
                    text = stringResource(R.string.balance_label) + if (state.userViewsFiat) {
                        " " + mainState.selectedCurrency.symbol + mainState.fiatBalanceFormatted
                    } else {
                        "  Ł" + mainState.litoshiBalance
                    },
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Start,
                        color = if (state.darkMode) Color.White else Color.Black
                    )
                )
                Spacer(modifier = Modifier.weight(1f))
            }
            Row {
                Text(
                    modifier = Modifier.padding(
                        start = 14.dp,
                        bottom = 8.dp
                    ),
                    text = stringResource(R.string.exchange_rate_label) + " " + stringResource(
                        R.string.Login_ltcPrice,
                        mainState.formattedCurrency
                    ),
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Start,
                        color = if (state.darkMode) Color.White else Color.Black
                    ),
                )
                Spacer(modifier = Modifier.weight(1f))
            }
            SendContinueButton(
                modifier = modifier.padding(
                    start = 12.dp,
                    end = 12.dp,
                    bottom = bottomButtonPadding
                ),
                darkMode = state.darkMode,
                enabled = !sendDataIsReady,
                onClick = {
                    if (pagerState.targetPage == 0) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(1)
                        }
                    } else {
                        onEvent(SendEvent.OnConfirmSend(viewModel.state.value.amountInLTC))
                    }
                }
            ) {
                if (pagerState.targetPage == 0) {
                    Text(stringResource(R.string.continue_cta))
                } else {
                    Text(stringResource(R.string.send_title))
                }
            }
        }
    }
}
