package com.brainwallet.ui.screens.send

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.brainwallet.R
import com.brainwallet.ui.composable.EditSendButton
import com.brainwallet.ui.composable.passcode.PasscodeIndicator
import org.koin.compose.viewmodel.koinViewModel
import com.brainwallet.ui.composable.passcode.PasscodeKeypad
import com.brainwallet.ui.composable.passcode.PasscodeKeypadEvent
import com.grunt.brainwallet.core.presentation.component.AnimatedSuccessBadge

@Composable
fun PasscodeConfirmWidget(
    onEdit: () -> Unit,
    onSentTransaction: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SendViewModel = koinViewModel(),
    onEvent: (PasscodeKeypadEvent) -> Unit = {}
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedContent(
                targetState = state.isPasscodeAuthenticated,
                transitionSpec = {
                    fadeIn(tween(400)) togetherWith fadeOut(tween(400))
                }
            ) { isAuthenticated ->
                if (isAuthenticated) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        AnimatedSuccessBadge(
                            size = 100.dp,
                            onFinished = {
                                onSentTransaction(true)
                            }
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        PasscodeIndicator(
                            state.passcode,
                            modifier = Modifier.weight(1f).padding(24.dp)
                        )
                        PasscodeKeypad { passcodeKeypadEvent ->
                            when (passcodeKeypadEvent) {
                                PasscodeKeypadEvent.OnDelete -> viewModel.onEvent(SendEvent.OnPasscodeDigitDeleted)
                                is PasscodeKeypadEvent.OnPressed -> viewModel.onEvent(
                                    SendEvent.OnPasscodeDigitAdded(passcodeKeypadEvent.digit)
                                )
                            }
                        }
                        EditSendButton(
                            modifier = Modifier.padding(8.dp),
                            darkMode = state.darkMode,
                            onClick = { onEdit() }
                        ) {
                            Text(stringResource(R.string.back))
                        }
                    }
                }
            }
        }
    }
}
