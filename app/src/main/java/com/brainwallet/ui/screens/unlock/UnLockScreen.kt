@file:OptIn(ExperimentalMaterial3Api::class)

package com.brainwallet.ui.screens.unlock

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.brainwallet.R
import com.brainwallet.data.model.AppSetting
import com.brainwallet.navigation.OnNavigate
import com.brainwallet.navigation.UiEffect
import com.brainwallet.ui.composable.BrainwalletScaffold
import com.brainwallet.ui.bentosections.buyreceivebento.receive.ReceiveDialogFragment
import com.brainwallet.ui.screens.unlock.components.UnLockScreenBody
import com.brainwallet.ui.screens.unlock.components.UnLockScreenFooter
import com.brainwallet.ui.screens.unlock.components.UnLockScreenHeader
import com.brainwallet.ui.theme.BrainwalletAppTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun UnLockScreen(
    onNavigate: OnNavigate,
    modifier: Modifier = Modifier,
    isUpdatePin: Boolean = false,
    viewModel: UnLockViewModel = koinViewModel()
) {
    val activity = LocalContext.current as? Activity
    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
    val fragmentManager = (LocalContext.current as? FragmentActivity)?.supportFragmentManager
    val uiState by viewModel.state.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.onEvent(UnLockEvent.OnLoad(isUpdatePin))
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is UiEffect.Navigate -> onNavigate.invoke(effect)
                is UiEffect.ShowMoonPayDialog -> fragmentManager?.let {
                    ReceiveDialogFragment.show(it)
                }
                else -> Unit
            }
        }
    }
    UnLockScreen(uiState = uiState, modifier = modifier, onEvent = viewModel::onEvent)
}

@Composable
private fun UnLockScreen(
    uiState: UnLockState,
    modifier: Modifier = Modifier,
    onEvent: (UnLockEvent) -> Unit = {},
) {
    LaunchedEffect(uiState.passcode.all { it > -1 }) {
        //
    }
    val horizontalVerticalSpacing = 8
    BrainwalletScaffold(
        modifier = modifier,
        topBar = {
            UnLockScreenHeader(
                modifier = Modifier.padding(16.dp),
                formattedLtcPrice = stringResource(
                    R.string.Login_ltcPrice,
                    uiState.formattedCurrency
                ),
            )
        },
        bottomBar = {
            UnLockScreenFooter(
                uiState.formattedVersion,
                modifier = Modifier.padding(bottom = 26.dp),
                onEvent = onEvent
            )
        }
    ) { paddingValues ->
        UnLockScreenBody(
            passcode = uiState.passcode,
            isUpdatePin = uiState.isUpdatePin,
            verticalArrangement = Arrangement.spacedBy(horizontalVerticalSpacing.dp),
            modifier = Modifier.padding(paddingValues)
                .padding(top = 16.dp),
            onEvent = onEvent
        )
    }
}

@PreviewLightDark
@Composable
private fun UnLockScreenPreview() {
    BrainwalletAppTheme(appSetting = AppSetting(isDarkMode = isSystemInDarkTheme())) {
        UnLockScreen(
            uiState = UnLockState(
                isUpdatePin = true,
                formattedCurrency = "$90.00",
                formattedVersion = "v4.0.0 (202501201)"
            )
        )
    }
}
