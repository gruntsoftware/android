@file:OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)

package com.brainwallet.ui.screens.home.receive

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.brainwallet.R
import com.brainwallet.data.model.getFormattedText
import com.brainwallet.data.model.isCustom
import com.brainwallet.navigation.LegacyNavigation
import com.brainwallet.navigation.UiEffect
import com.brainwallet.ui.composable.MoonpayBuyButton
import com.brainwallet.ui.composable.VerticalWheelPicker
import com.brainwallet.ui.composable.WheelPickerFocusVertical
import com.brainwallet.ui.composable.rememberWheelPickerState
import com.brainwallet.ui.theme.BrainwalletAppTheme
import com.brainwallet.ui.theme.BrainwalletTheme
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import org.koin.android.ext.android.inject
import org.koin.compose.koinInject
import timber.log.Timber

@Composable
fun ReceiveDialog(
    onDismissRequest: () -> Unit,
    viewModel: ReceiveDialogViewModel = koinInject()
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

    LaunchedEffect(Unit) {
        delay(500)
        wheelPickerFiatCurrencyState.scrollToIndex(state.getSelectedFiatCurrencyIndex())
    }

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

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.bottom_nav_item_buy_receive_title).uppercase(),
                style = BrainwalletTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = BrainwalletTheme.colors.surface
                ),
            )
            OutlinedIconButton(
                modifier = Modifier.align(Alignment.TopEnd),
                border = null,
                onClick = onDismissRequest
            ) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = stringResource(R.string.AccessibilityLabels_close),
                    tint = BrainwalletTheme.colors.surface
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            state.qrBitmap?.asImageBitmap()?.let { imageBitmap ->
                Image(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    bitmap = imageBitmap,
                    contentDescription = "address"
                )
            } ?: Box(
                modifier = Modifier
                    .weight(1f)
                    .height(160.dp)
                    .background(Color.Gray)
                    .padding(8.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = state.address,
                    style = BrainwalletTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = BrainwalletTheme.colors.surface
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
                        style = BrainwalletTheme.typography.bodySmall.copy(
                            color = BrainwalletTheme.colors.surface,
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedIconButton(
                        modifier = Modifier.size(32.dp),
                        onClick = {
                            viewModel.onEvent(ReceiveDialogEvent.OnCopyClick(context))
                            Toast.makeText(context, R.string.Receive_copied, Toast.LENGTH_SHORT)
                                .show()
                        },
                        colors = IconButtonDefaults.outlinedIconButtonColors(
                            containerColor = BrainwalletTheme.colors.content.copy(alpha = 0.5f)
                        ),
                    ) {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_copy),
                            contentDescription = stringResource(R.string.URLHandling_copy),
                            tint = BrainwalletTheme.colors.surface
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
                        dividerColor = BrainwalletTheme.colors.surface.copy(
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
                    color = BrainwalletTheme.colors.surface
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.getLtcAmountFormatted(loadingState.visible),
                    style = BrainwalletTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = BrainwalletTheme.colors.surface
                    )
                )
                Text(
                    text = state.getRatesUpdatedAtFormatted(),
                    style = BrainwalletTheme.typography.bodySmall.copy(
                        color = BrainwalletTheme.colors.surface
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
                        viewModel.onEvent(
                            ReceiveDialogEvent.OnFiatAmountOptionIndexChange(
                                index,
                                quickFiatAmountOption
                            )
                        )
                    },
                    label = {
                        Text(
                            text = if (quickFiatAmountOption.isCustom())
                                stringResource(R.string.custom)
                            else quickFiatAmountOption.getFormattedText()
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
                prefix = {
                    Text(
                        text = state.selectedFiatCurrency.symbol,
                        style = BrainwalletTheme.typography.bodyMedium.copy(color = BrainwalletTheme.colors.surface)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = {
                        viewModel.onEvent(ReceiveDialogEvent.OnFiatAmountChange(state.fiatAmount))
                    }) {
                        Icon(Icons.Default.Done, contentDescription = null)
                    }
                },
                textStyle = BrainwalletTheme.typography.bodyMedium.copy(color = BrainwalletTheme.colors.surface),
                value = "${if (state.fiatAmount < 1) "" else state.fiatAmount}",
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                onValueChange = { input ->
                    val amount = input.toFloatOrNull() ?: 0f
                    viewModel.onEvent(ReceiveDialogEvent.OnFiatAmountChange(amount, false))
                },
                shape = BrainwalletTheme.shapes.large,
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
                LegacyNavigation.showMoonPayWidget(
                    context = context,
                    params = mapOf(
                        "baseCurrencyCode" to state.selectedFiatCurrency.code,
                        "baseCurrencyAmount" to state.fiatAmount.toString(),
                        "language" to appSetting.languageCode,
                        "walletAddress" to state.address
                    )
                )
                onDismissRequest.invoke()
            },
        )
    }
}

/**
 * describe [ReceiveDialogFragment] for backward compat,
 * since we are still using [com.brainwallet.presenter.activities.BreadActivity]
 */
class ReceiveDialogFragment : DialogFragment() {

    private val viewModel: ReceiveDialogViewModel by inject()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                val appSetting by viewModel.appSetting.collectAsState()
                /**
                 * we need this theme inside this fragment,
                 * because we are still using fragment to display ReceiveDialog composable
                 * pls check BreadActivity.handleNavigationItemSelected
                 */
                BrainwalletAppTheme(appSetting = appSetting) {
                    Box(
                        modifier = Modifier
                            .padding(24.dp)
                            .background(
                                BrainwalletTheme.colors.content,
                                shape = BrainwalletTheme.shapes.extraLarge
                            )
                            .border(
                                width = 1.dp,
                                color = BrainwalletTheme.colors.surface,
                                shape = BrainwalletTheme.shapes.extraLarge
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ReceiveDialog(
                            viewModel = viewModel,
                            onDismissRequest = { dismiss() }
                        )
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        isCancelable = false
    }

    companion object {
        @JvmStatic
        fun show(manager: FragmentManager) {
            ReceiveDialogFragment().show(manager, "ReceiveDialogFragment")
        }
    }
}