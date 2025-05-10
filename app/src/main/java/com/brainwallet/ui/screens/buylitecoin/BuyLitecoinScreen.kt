package com.brainwallet.ui.screens.buylitecoin

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.brainwallet.R
import com.brainwallet.navigation.LegacyNavigation
import com.brainwallet.navigation.OnNavigate
import com.brainwallet.navigation.UiEffect
import com.brainwallet.ui.composable.BrainwalletScaffold
import com.brainwallet.ui.composable.BrainwalletTopAppBar
import com.brainwallet.ui.composable.LargeButton
import com.brainwallet.ui.composable.LoadingDialog
import com.brainwallet.ui.theme.BrainwalletTheme
import org.koin.compose.koinInject

//TODO: wip
@Composable
fun BuyLitecoinScreen(
    onNavigate: OnNavigate,
    viewModel: BuyLitecoinViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val loadingState by viewModel.loadingState.collectAsState()
    val appSetting by viewModel.appSetting.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onEvent(BuyLitecoinEvent.OnLoad(context))
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is UiEffect.ShowMessage ->
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()

                else -> Unit
            }
        }
    }

    BrainwalletScaffold(
        topBar = {
            BrainwalletTopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = { onNavigate.invoke(UiEffect.Navigate.Back()) },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        Box(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = stringResource(R.string.buy_litecoin_title),
                    style = MaterialTheme.typography.titleLarge,
                )

                OutlinedTextField(
                    prefix = {
                        Text(
                            text = appSetting.currency.symbol,
                            style = BrainwalletTheme.typography.titleLarge.copy(color = BrainwalletTheme.colors.content)
                        )
                    },
                    textStyle = BrainwalletTheme.typography.titleLarge.copy(color = BrainwalletTheme.colors.content),
                    value = "${if (state.fiatAmount < 1) "" else state.fiatAmount}",
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    onValueChange = { input ->
                        val amount = input.toFloatOrNull() ?: 0f
                        viewModel.onEvent(BuyLitecoinEvent.OnFiatAmountChange(amount))
                    },
                    shape = BrainwalletTheme.shapes.extraLarge,
                    isError = state.isValid().not(),
                    supportingText = {
                        state.errorStringId?.let {
                            Text(stringResource(it, state.fiatAmount))
                        }
                    }
                )

                Text(
                    text = state.getLtcAmountFormatted(loadingState.visible),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = BrainwalletTheme.colors.content.copy(
                            0.7f
                        )
                    ),
                )

                Text(
                    text = stringResource(R.string.buy_litecoin_desc),
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = state.address,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = BrainwalletTheme.colors.content.copy(
                            0.7f
                        )
                    )
                )

            }


            LargeButton(
                modifier = Modifier.align(Alignment.BottomCenter),
                enabled = state.isValid(),
                onClick = {
                    LegacyNavigation.showMoonPayWidget(
                        context = context,
                        params = mapOf(
                            "baseCurrencyCode" to appSetting.currency.code,
                            "baseCurrencyAmount" to state.fiatAmount.toString(),
                            "language" to appSetting.languageCode,
                            "walletAddress" to state.address
                        )
                    )
                }
            ) {
                Text(stringResource(R.string.buy_litecoin_button_moonpay))
            }
        }


    }

    if (loadingState.visible) {
        LoadingDialog()
    }
}