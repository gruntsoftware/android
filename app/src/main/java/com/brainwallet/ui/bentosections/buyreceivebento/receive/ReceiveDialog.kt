@file:OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)

package com.brainwallet.ui.bentosections.buyreceivebento.receive

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AssistChip
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.brainwallet.R
import com.brainwallet.constants.bentoBorderWidth
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.model.getFormattedText
import com.brainwallet.data.model.isCustom
import com.brainwallet.navigation.MoonPayWidgetLauncher
import com.brainwallet.navigation.MoonPayWidgetLauncherViewModel
import com.brainwallet.navigation.UiEffect
import com.brainwallet.ui.LoadingState
import com.brainwallet.ui.composable.MoonpayBuyButton
import com.brainwallet.ui.composable.VerticalWheelPicker
import com.brainwallet.ui.composable.WheelPickerState
import com.brainwallet.ui.composable.rememberWheelPickerState
import com.brainwallet.ui.theme.BrainwalletAppTheme
import com.brainwallet.ui.theme.DesignTheme
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import org.koin.compose.viewmodel.koinViewModel
import timber.log.Timber
import com.brainwallet.ui.composable.WheelPickerFocusVertical

@Composable
fun ReceiveDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ReceiveDialogViewModel = koinViewModel(),
    moonPayWidgetLauncherViewModel: MoonPayWidgetLauncherViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val loadingState by viewModel.loadingState.collectAsState()
    val appSetting by viewModel.appSetting.collectAsState()
    val context = LocalContext.current
    val wheelPickerFiatCurrencyState = rememberWheelPickerState(0)
    LaunchedEffect(Unit) {
        viewModel.onEvent(ReceiveDialogEvent.OnLoad(context))
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is UiEffect.ShowMessage -> Toast.makeText(
                    context,
                    effect.message,
                    Toast.LENGTH_SHORT
                ).show()

                else -> Unit
            }
        }
    }
    // Set the initial index to the selected fiat currency
    LaunchedEffect(Unit) {
        delay(500)
        wheelPickerFiatCurrencyState.scrollToIndex(state.getSelectedFiatCurrencyIndex())
    }
    // Listen for changes in the selected fiat currency index
    LaunchedEffect(wheelPickerFiatCurrencyState) {
        snapshotFlow { wheelPickerFiatCurrencyState.currentIndex }
            .filter { it > -1 }
            .distinctUntilChanged()
            .debounce(700)
            .collect {
                Timber.i("wheelPickerFiatCurrencyState: currentIndex $it")

                viewModel.onEvent(ReceiveDialogEvent.OnFiatCurrencyChange(state.fiatCurrencies[it]))
            }
    }
    ReceiveDialog(
        state = state,
        loadingState = loadingState,
        modifier = modifier,
        appSetting = appSetting,
        wheelPickerFiatCurrencyState = wheelPickerFiatCurrencyState,
        onDismissRequest = onDismissRequest,
        onMoonPayLaunch = moonPayWidgetLauncherViewModel::launch,
        onEvent = viewModel::onEvent
    )
    MoonPayWidgetLauncher(
        viewModel = moonPayWidgetLauncherViewModel,
        onResult = onDismissRequest
    )
}

@Composable
private fun ReceiveDialog(
    state: ReceiveDialogState,
    modifier: Modifier = Modifier,
    appSetting: AppSetting = AppSetting(),
    loadingState: LoadingState = LoadingState(),
    wheelPickerFiatCurrencyState: WheelPickerState = rememberWheelPickerState(0),
    onDismissRequest: () -> Unit = {},
    onMoonPayLaunch: (Map<String, String>) -> Unit = {},
    onEvent: (ReceiveDialogEvent) -> Unit = {}
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .background(
                DesignTheme.colors.content,
                shape = DesignTheme.shapes.large
            )
            .border(
                width = bentoBorderWidth,
                color = DesignTheme.colors.surface,
                shape = DesignTheme.shapes.large
            ),
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DesignTheme.colors.content // invert surface
                ),
                expandedHeight = 56.dp,
                title = {
                    Text(
                        text = stringResource(R.string.bottom_nav_item_buy_receive_title).uppercase(),
                        style = DesignTheme.typography.titleSmall.copy(
                            color = DesignTheme.colors.surface
                        )
                    )
                },
                navigationIcon = {
                    if (state.moonpayWidgetVisible()) {
                        IconButton(onClick = {
                            onEvent(ReceiveDialogEvent.OnSignedUrlClear)
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        modifier = Modifier.testTag("buttonClose"),
                        onClick = onDismissRequest
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = stringResource(R.string.AccessibilityLabels_close),
                            tint = DesignTheme.colors.surface
                        )
                    }
                }
            )

            // moonpay widget
            // todo: revisit this later
//        AnimatedVisibility(visible = state.moonpayWidgetVisible()) {
//            state.moonpayBuySignedUrl?.let { signedUrl ->
//                MoonpayBuyWidget(
//                    modifier = Modifier.height(500.dp),
//                    signedUrl = signedUrl
//                )
//            }
//        }

            // buy / receive
//        AnimatedVisibility(visible = state.moonpayWidgetVisible().not()) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    state.qrBitmap?.asImageBitmap()?.let { imageBitmap ->
                        Image(
                            modifier = Modifier
                                .weight(1f),
                            bitmap = imageBitmap,
                            contentDescription = "address",
                            colorFilter = ColorFilter.tint(DesignTheme.colors.surface)
                        )
                    } ?: Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(180.dp)
                            .background(Color.Gray)
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = state.address,
                            style = DesignTheme.typography.bodyLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = DesignTheme.colors.surface
                            ),
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.new_address).uppercase(),
                                style = DesignTheme.typography.bodySmall.copy(
                                    color = DesignTheme.colors.surface,
                                ),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedIconButton(
                                modifier = Modifier.size(32.dp),
                                onClick = {
                                    onEvent(ReceiveDialogEvent.OnCopyClick(context))
                                    Toast.makeText(
                                        context,
                                        R.string.Receive_copied,
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                },
                                colors = IconButtonDefaults.outlinedIconButtonColors(
                                    containerColor = DesignTheme.colors.content.copy(alpha = 0.5f)
                                ),
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_copy),
                                    contentDescription = stringResource(R.string.URLHandling_copy),
                                    tint = DesignTheme.colors.surface
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    VerticalWheelPicker(
                        modifier = Modifier.weight(1f),
                        focus = {
                            WheelPickerFocusVertical(
                                dividerColor = DesignTheme.colors.surface.copy(
                                    alpha = 0.5f
                                )
                            )
                        },
                        unfocusedCount = 1,
                        count = state.fiatCurrencies.size,
                        state = wheelPickerFiatCurrencyState,
                    ) { index ->
                        Text(
                            text = state.fiatCurrencies[index].code,
                            fontWeight = FontWeight.Bold,
                            color = DesignTheme.colors.surface
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.getLtcAmountFormatted(loadingState.visible),
                            style = DesignTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = DesignTheme.colors.surface
                            )
                        )
                        Text(
                            text = state.getRatesUpdatedAtFormatted(),
                            style = DesignTheme.typography.bodySmall.copy(
                                color = DesignTheme.colors.surface
                            )
                        )
                    }
                }

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(items = state.getQuickFiatAmountOptions()) { index, quickFiatAmountOption ->
                        AssistChip(
                            enabled = loadingState.visible.not(),
                            onClick = {
                                onEvent(
                                    ReceiveDialogEvent.OnFiatAmountOptionIndexChange(
                                        index,
                                        quickFiatAmountOption
                                    )
                                )
                            },
                            label = {
                                Text(
                                    text = if (quickFiatAmountOption.isCustom()) {
                                        stringResource(R.string.custom)
                                    } else {
                                        quickFiatAmountOption.getFormattedText()
                                    },
                                    style = DesignTheme.typography.bodyMedium.copy(
                                        color = DesignTheme.colors.surface
                                    )
                                )
                            },
                            leadingIcon = {
                                if (index == state.selectedQuickFiatAmountOptionIndex) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                }
                            }
                        )
                    }
                }

                AnimatedVisibility(visible = state.isQuickFiatAmountOptionCustom()) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        prefix = {
                            Text(
                                text = state.selectedFiatCurrency.symbol,
                                style = DesignTheme.typography.bodyMedium.copy(
                                    color = DesignTheme.colors.surface
                                )
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = {
                                onEvent(ReceiveDialogEvent.OnFiatAmountChange(state.fiatAmount))
                            }) {
                                Icon(Icons.Default.Done, contentDescription = null)
                            }
                        },
                        textStyle = DesignTheme.typography.bodyMedium.copy(
                            color = DesignTheme.colors.surface
                        ),
                        value = "${if (state.fiatAmount < 1) "" else state.fiatAmount}",
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        onValueChange = { input ->
                            val amount = input.toFloatOrNull() ?: 0f
                            onEvent(ReceiveDialogEvent.OnFiatAmountChange(amount, false))
                        },
                        shape = DesignTheme.shapes.large,
                        isError = state.errorFiatAmountStringId != null,
                        supportingText = {
                            state.errorFiatAmountStringId?.let {
                                Text(stringResource(it, state.fiatAmount))
                            }
                        }
                    )
                }

                MoonpayBuyButton(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = loadingState.visible.not(),
                    onClick = {
                        // todo: revisit this later
                        // viewModel.onEvent(ReceiveDialogEvent.OnMoonpayButtonClick)
                        onMoonPayLaunch(
                            mapOf(
                                "baseCurrencyCode" to state.selectedFiatCurrency.code,
                                "baseCurrencyAmount" to state.fiatAmount.toString(),
                                "language" to appSetting.languageCode,
                                "walletAddress" to state.address,
                            )
                        )
                    },
                )

//            }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ReceiveDialogPreview() {
    val appSetting = AppSetting(isDarkMode = isSystemInDarkTheme())
    BrainwalletAppTheme(appSetting) {
        ReceiveDialog(
            modifier = Modifier.padding(12.dp),
            state = ReceiveDialogState(),
            appSetting = appSetting
        )
    }
}
