package com.brainwallet.ui.screen.setpasscode

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SetPasscodeMainScreen(
    onBackClick: () -> Unit,
    digits: List<Int>,
    viewModel: SetPasscodeMainViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    AnimatedVisibility(
        visible = state.isScrenStep(ScreenStep.ReadyScreen)
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
        visible = state.isScrenStep(ScreenStep.SetPasscodeScreen)
    ) {
        SetPasscodeScreen(
            viewModel = viewModel,
            onEvent = { event ->
                when (event) {
                    SetPasscodeMainEvent.OnBackClick -> viewModel.onEvent(
                        SetPasscodeMainEvent.OnNavigateStepTo(
                            ScreenStep.ReadyScreen
                        )
                    )

                    else -> Unit
                }
            }, digits = digits
        )
    }

    AnimatedVisibility(
        visible = state.isScrenStep(ScreenStep.ConfirmScreen)
    ) {
        SetPasscodeConfirmScreen(
            viewModel = viewModel,
            digits = digits,
        )
    }


}