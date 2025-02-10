package com.brainwallet.ui.screen.setpasscode

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SetPasscodeMainScreen(
    onBackClick: () -> Unit,
    digits: List<Int>,
    viewModel: SetPasscodeMainViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
      
    AnimatedVisibility(
        visible = state.isScreenStep(ScreenStep.ReadyScreen),
        enter = slideInHorizontally(),
        exit = slideOutHorizontally()
    ) {
        SetPasscodeReadyScreen(
            viewModel = viewModel,
            onReadyClick = {
                viewModel.onEvent(SetPasscodeMainEvent.OnNavigateStepTo(ScreenStep.SetPasscodeScreen))
            },
            onBackClick = onBackClick
        )
    }

    AnimatedVisibility(
        visible = state.isScreenStep(ScreenStep.SetPasscodeScreen),
        enter = slideInHorizontally(),
        exit = slideOutHorizontally()
    ) {
        SetPasscodeScreen(
            viewModel = viewModel,
            digits = digits,
            onEvent = { event ->
                when (event) {
                    SetPasscodeMainEvent.OnBackClick -> viewModel.onEvent(
                        SetPasscodeMainEvent.OnNavigateStepTo(
                            ScreenStep.ReadyScreen
                        )
                    )
                    else -> Unit
                }
            }
        )
    }

    AnimatedVisibility(
        visible = state.isScreenStep(ScreenStep.ConfirmScreen),
        enter = slideInHorizontally(),
        exit = slideOutHorizontally()
    ) {
        SetPasscodeConfirmScreen(
            viewModel = viewModel,
            digits = digits,
        )
    }
}
