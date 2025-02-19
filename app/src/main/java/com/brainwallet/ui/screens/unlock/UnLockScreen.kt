@file:OptIn(ExperimentalMaterial3Api::class)

package com.brainwallet.ui.screens.unlock


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brainwallet.R
import com.brainwallet.navigation.OnNavigate
import com.brainwallet.navigation.UiEffect
import com.brainwallet.tools.util.BRConstants
import com.brainwallet.ui.composable.BrainwalletScaffold
import com.brainwallet.ui.composable.BrainwalletTopAppBar
import com.brainwallet.ui.composable.PasscodeIndicator
import com.brainwallet.ui.composable.PasscodeKeypad
import com.brainwallet.ui.composable.PasscodeKeypadEvent
import com.brainwallet.ui.theme.BrainwalletTheme

//TODO: WIP here
@Composable
fun UnLockScreen(
    onNavigate: OnNavigate,
    viewModel: UnLockViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onEvent(UnLockEvent.OnLoad(context))
    }

    LaunchedEffect(state.passcode.all { it > -1 }) {
        //
    }

    /// Layout values
    val columnPadding = 18
    val horizontalVerticalSpacing = 8
    val spacerHeight = 48
    val maxItemsPerRow = 3

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
                })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(columnPadding.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(horizontalVerticalSpacing.dp),
        ) {

            // TODO: pass system dark mode preference
            // painter = painterResource(if (state.darkMode) R.drawable.bw_white_logotype else R.drawable.bw_logotype ),
            Image(
                modifier = Modifier.fillMaxWidth(0.80f)
                    .padding(horizontalVerticalSpacing.dp),
                painter = painterResource(R.drawable.bw_white_logotype),
                contentDescription = "logo"
            )

            Text(stringResource(R.string.Login_ltcPrice, state.formattedCurrency))
            Text(stringResource(R.string.Login_currentLtcPrice, state.iso))

//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(100.dp)
//                    .background(Color.White)
//            ) {
//                //todo
//                Text("todo")
//            }

            Spacer(Modifier.height(16.dp))

            PasscodeIndicator(passcode = state.passcode)

            Spacer(Modifier.height(16.dp))

            PasscodeKeypad { passcodeKeypadEvent ->
                when (passcodeKeypadEvent) {
                    PasscodeKeypadEvent.OnDelete -> viewModel.onEvent(UnLockEvent.OnDeletePinDigit)
                    is PasscodeKeypadEvent.OnPressed -> viewModel.onEvent(
                        UnLockEvent.OnPinDigitChange(
                            digit = passcodeKeypadEvent.digit
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            //app version
            Text(
                text = BRConstants.APP_VERSION_NAME_CODE,
                style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center)
            )
        }
    }
}