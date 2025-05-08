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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.brainwallet.R
import com.brainwallet.navigation.LegacyNavigation
import com.brainwallet.navigation.UiEffect
import com.brainwallet.ui.composable.BrainwalletButton
import com.brainwallet.ui.composable.LoadingDialog
import com.brainwallet.ui.composable.MoonpayBuyButton
import com.brainwallet.ui.composable.VerticalWheelPicker
import com.brainwallet.ui.composable.rememberWheelPickerState
import com.brainwallet.ui.theme.BrainwalletAppTheme
import com.brainwallet.ui.theme.BrainwalletTheme
import kotlinx.coroutines.FlowPreview
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
    val wheelPickerAmountState = rememberWheelPickerState(0)

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

    LaunchedEffect(loadingState) {
        if (loadingState.visible.not()) {
            wheelPickerFiatCurrencyState.animateScrollToIndex(state.getSelectedFiatCurrencyIndex())
            wheelPickerAmountState.animateScrollToIndex(state.getWheelPickerFiatAmountIndex())
        }
    }

    LaunchedEffect(wheelPickerAmountState) {
        snapshotFlow { wheelPickerAmountState.currentIndex }
            .debounce(2000)
            .distinctUntilChanged()
            .filter { it > -1 }
            .collect {
                Timber.i("wheelPickerAmountState: currentIndex $it")

                viewModel.onEvent(
                    ReceiveDialogEvent.OnFiatAmountChange(
                        state.getWheelPickerAmountFor(it).toFloat()
                    )
                )
            }
    }

    LaunchedEffect(wheelPickerFiatCurrencyState) {
        snapshotFlow { wheelPickerFiatCurrencyState.currentIndex }
            .debounce(700)
            .distinctUntilChanged()
            .filter { it > -1 }
            .collect {
                Timber.i("wheelPickerFiatCurrencyState: currentIndex $it")

                viewModel.onEvent(ReceiveDialogEvent.OnFiatCurrencyChange(state.fiatCurrencies[it]))
            }
    }

    AnimatedVisibility(loadingState.visible) {
        LoadingDialog()
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
                text = stringResource(R.string.Receive_title).uppercase(),
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
                    )
                )
                Text(
                    text = stringResource(R.string.new_address).uppercase(),
                    style = BrainwalletTheme.typography.bodySmall.copy(
                        color = BrainwalletTheme.colors.surface,
                    )
                )
            }
        }
        BrainwalletButton(
            shape = BrainwalletTheme.shapes.large,
            onClick = {
                viewModel.onEvent(ReceiveDialogEvent.OnCopyClick(context))
                Toast.makeText(context, R.string.Receive_copied, Toast.LENGTH_SHORT).show()
            }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(R.string.URLHandling_copy).uppercase(),
                    fontWeight = FontWeight.Bold
                )
                Text(state.address)
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
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

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            VerticalWheelPicker(
                modifier = Modifier.weight(.5f),
                unfocusedCount = 1,
                count = state.getWheelPickerAmountSize(),
                state = wheelPickerAmountState
            ) { index ->
                Text(
                    text = state.getWheelPickerAmountFor(index).toString(),
                    fontWeight = FontWeight.Bold,
                    overflow = TextOverflow.Ellipsis
                )
            }

            VerticalDivider(modifier = Modifier.height(40.dp))

            VerticalWheelPicker(
                modifier = Modifier.weight(.5f),
                unfocusedCount = 1,
                count = state.fiatCurrencies.size,
                state = wheelPickerFiatCurrencyState,
            ) { index ->
                Text(state.fiatCurrencies[index].code, fontWeight = FontWeight.Bold)
            }

            MoonpayBuyButton(
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
                modifier = Modifier.weight(1f),
                enabled = loadingState.visible.not()
            )
        }
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
                        ReceiveDialog(onDismissRequest = { dismiss() })
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
    }

    companion object {
        @JvmStatic
        fun show(manager: FragmentManager) {
            ReceiveDialogFragment().show(manager, "ReceiveDialogFragment")
        }
    }
}