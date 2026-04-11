package com.brainwallet.ui.screens.send

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.R
import com.brainwallet.navigation.OnNavigate
import com.brainwallet.navigation.UiEffect
import com.brainwallet.ui.composable.SendContinueButton
import com.brainwallet.ui.screens.main.MainViewModel
import com.brainwallet.ui.theme.DesignTheme
import org.koin.compose.viewmodel.koinViewModel
import com.brainwallet.ui.theme.IBMPlexSans
import kotlinx.coroutines.launch
import timber.log.Timber

@Composable
fun SendScreen(
    onNavigate: OnNavigate,
    modifier: Modifier = Modifier,
    onOpenScanner: () -> Unit = {},
    onDimissSendModal: () -> Unit = {},
    viewModel: SendViewModel = koinViewModel(),
    mainViewModel: MainViewModel = koinViewModel()
) {
    val uiState by viewModel.state.collectAsState()
    val context = LocalContext.current
    SendScreen(
        uiState = uiState,
        onNavigate = onNavigate,
        modifier = modifier,
        onEvent = viewModel::onEvent,
        onOpenScanner = onOpenScanner,
        onDimissSendModal = onDimissSendModal
    )
}

@Composable
private fun SendScreen(
    uiState: SendState,
    onNavigate: OnNavigate,
    modifier: Modifier = Modifier,
    onOpenScanner: () -> Unit = {},
    onDimissSendModal: () -> Unit = {},
    onEvent: (SendEvent) -> Unit = {},
    viewModel: SendViewModel = koinViewModel(),
    mainViewModel: MainViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val mainState by mainViewModel.state.collectAsState()
    val context = LocalContext.current
    val bottomButtonPadding = 60.dp
    val modalFactor = 0.85f
    val pageCount = 3
    val pagerState = rememberPagerState(pageCount = { pageCount })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.onEvent(SendEvent.OnLoad)
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is UiEffect.Navigate -> onNavigate.invoke(effect)
                is UiEffect.OpenQRScanner -> onOpenScanner()
                is UiEffect.OnEnterAuthPasscode ->
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(2)
                    }
                else -> Unit
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                modifier = Modifier
                    .weight(0.12f)
                    .padding(14.dp),
                text = if (pagerState.targetPage == 0) {
                    stringResource(R.string.send_litecoin_label)
                } else if (pagerState.targetPage == 1) {
                    stringResource(
                        R.string.confirm_send_label
                    )
                } else {
                    stringResource(
                        R.string.enter_pin
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
                userScrollEnabled = false,
                modifier = Modifier
                    .weight(modalFactor)
            ) { page ->
                if (page == 0) {
                    PreSend(
                        modifier = Modifier.weight(modalFactor)
                    )
                } else if (page == 1) {
                    ConfirmSend(
                        modifier = Modifier.weight(modalFactor),
                        onEdit = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(0)
                            }
                        }
                    )
                } else {
                    PasscodeConfirmWidget(
                        modifier = Modifier.weight(modalFactor),
                        onSentTransaction = {
                            onDimissSendModal()
                        },
                        onEdit = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(1)
                            }
                        }
                    )
                }
            }
            Text(
                modifier = Modifier
                    .weight(0.05f)
                    .padding(4.dp),
                text = if (state.isReadyToSend) {
                    " "
                } else {
                    stringResource(
                        R.string.send_error_message
                    )
                },
                style = TextStyle(
                    fontFamily = IBMPlexSans,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = if (state.darkMode) Color.White else Color.Black
                )
            )
            Spacer(modifier = Modifier.weight(0.01f))

            Row(
                modifier = Modifier
                    .weight(0.04f)
            ) {
                Text(
                    modifier = Modifier
                        .padding(
                            start = 16.dp
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
                Text(
                    modifier = Modifier.padding(
                        end = 16.dp,
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
            }
            SendContinueButton(
                modifier = Modifier
                    .weight(0.22f)
                    .padding(
                        start = 12.dp,
                        end = 12.dp,
                        bottom = bottomButtonPadding
                    ),
                darkMode = state.darkMode,
                enabled = when (pagerState.currentPage) {
                    0 -> state.isLTCAddressValid && state.isAmountBelowBalance
                    1 -> state.isReadyToSend
                    2 -> state.isPasscodeAuthenticated
                    else -> false
                },
                onClick = {
                    when (pagerState.currentPage) {
                        0 -> coroutineScope.launch { pagerState.animateScrollToPage(1) }
                        1 -> {
                            onEvent(SendEvent.OnAuthPasscode)
                        }
                        2 -> {
                            Timber.d("OnSend triggered: transactionItem=${state.transactionItem}")
                            onEvent(SendEvent.OnSend(state.transactionItem))
                        }
                        else -> null
                    }
                }
            ) {
                when (pagerState.currentPage) {
                    0 -> Text(stringResource(R.string.continue_cta))
                    1 -> Text(stringResource(R.string.ok))
                    2 -> Text(stringResource(R.string.send_title))
                    else -> null
                }
            }
        }
    }
}

@Composable
@PreviewLightDark
fun SendScreenPreview() {
    DesignTheme(isSystemInDarkTheme()) {
        SendScreen(
            onNavigate = {},
        )
    }
}
