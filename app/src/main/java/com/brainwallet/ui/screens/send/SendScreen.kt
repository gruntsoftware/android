package com.brainwallet.ui.screens.send

import com.brainwallet.R
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.brainwallet.data.model.AppSetting
import com.brainwallet.navigation.OnNavigate
import com.brainwallet.navigation.UiEffect
import com.brainwallet.ui.composable.BrainwalletScaffold
import com.brainwallet.ui.theme.BrainwalletAppTheme
import com.brainwallet.ui.theme.DesignTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SendScreen(
    onNavigate: OnNavigate,
    modifier: Modifier = Modifier,
    viewModel: SendViewModel = koinViewModel()
) {
    val uiState by viewModel.state.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        viewModel.onEvent(SendEvent.OnLoad(context))
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
    viewModel: SendViewModel = koinViewModel()
) {
    val horizontalVerticalSpacing = 8
    val state by viewModel.state.collectAsState()
    val loadingState by viewModel.loadingState.collectAsState()
    val context = LocalContext.current
    BrainwalletScaffold { paddingValues ->
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
                    text = stringResource(R.string.send_title),
                    style = DesignTheme.typography.titleLarge,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun SendScreenPreview() {
    BrainwalletAppTheme(appSetting = AppSetting(isDarkMode = isSystemInDarkTheme())) {
        SendScreen(
            uiState = SendState()
        )
    }
}
