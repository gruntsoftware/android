@file:OptIn(ExperimentalMaterial3Api::class)

package com.brainwallet.ui.screens.setpasscode

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brainwallet.R
import com.brainwallet.navigation.OnNavigate
import com.brainwallet.navigation.UiEffect
import com.brainwallet.ui.composable.BrainwalletScaffold
import com.brainwallet.ui.composable.BrainwalletTopAppBar
import com.brainwallet.ui.composable.PasscodeIndicator
import com.brainwallet.ui.composable.PasscodeKeypad
import com.brainwallet.ui.composable.PasscodeKeypadEvent

@Composable
fun SetPasscodeScreen(
    onNavigate: OnNavigate,
    passcode: List<Int> = emptyList(),
    viewModel: SetPasscodeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onEvent(SetPasscodeEvent.OnLoad(passcode))
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is UiEffect.Navigate -> onNavigate.invoke(effect)
                else -> Unit
            }
        }
    }

    /// Layout values
    val leadingCopyPadding = 16

    val horizontalVerticalSpacing = 8
    val spacerHeight = 90
    val imageMedium = 80

    val headlineFontSize = 44
    val paragraphFontSize = 22
    val lineHeight = 35


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
        Spacer(modifier = Modifier.height(spacerHeight.dp))
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(leadingCopyPadding.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(horizontalVerticalSpacing.dp),
        ) {
            Text(
                text = stringResource(if (state.isConfirm) R.string.confirm else R.string.setup_app_passcode),
                style = MaterialTheme.typography.headlineSmall,
            )

            if (state.isConfirm) {
                Text(
                    text = stringResource(R.string.confirm_desc),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    ),
                )
            } else {
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(R.string.setup_app_details_1))
                        append("\n")
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                            )
                        ) {
                            append(stringResource(R.string.setup_app_details_2))
                        }
                        append("\n")
                        append(stringResource(R.string.setup_app_details_3))
                    },
                    style = MaterialTheme.typography.bodyLarge.copy(
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    ),
                )
            }
            Spacer(modifier = Modifier.weight(0.1f))

            PasscodeIndicator(passcode = if (state.isConfirm) state.passcodeConfirm else state.passcode)

            Spacer(modifier = Modifier.weight(0.1f))

            PasscodeKeypad { passcodeKeypadEvent ->
                when (passcodeKeypadEvent) {
                    PasscodeKeypadEvent.OnDelete -> viewModel.onEvent(SetPasscodeEvent.OnDeleteDigit)
                    is PasscodeKeypadEvent.OnPressed -> viewModel.onEvent(
                        SetPasscodeEvent.OnDigitChange(
                            passcodeKeypadEvent.digit
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.weight(0.05f))
        }

    }
}
