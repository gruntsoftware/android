@file:OptIn(ExperimentalMaterial3Api::class)

package com.brainwallet.ui.screens.unlock


import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.brainwallet.R
import com.brainwallet.navigation.OnNavigate
import com.brainwallet.navigation.UiEffect
import com.brainwallet.tools.manager.AnalyticsManager
import com.brainwallet.tools.security.AuthManager
import com.brainwallet.tools.util.BRConstants
import com.brainwallet.ui.composable.BrainwalletScaffold
import com.brainwallet.ui.composable.BrainwalletTopAppBar
import com.brainwallet.ui.composable.PasscodeIndicator
import com.brainwallet.ui.composable.PasscodeKeypad
import com.brainwallet.ui.composable.PasscodeKeypadEvent
import com.brainwallet.ui.theme.BrainwalletTheme
import org.koin.compose.koinInject

@Composable
fun UnLockScreen(
    onNavigate: OnNavigate,
    isUpdatePin: Boolean = false,
    viewModel: UnLockViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onEvent(UnLockEvent.OnLoad(context, isUpdatePin))
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is UiEffect.Navigate -> onNavigate.invoke(effect)
                else -> Unit
            }
        }
    }

    LaunchedEffect(state.passcode.all { it > -1 }) {
        //
    }

    /// Layout values
    val columnPadding = 18
    val horizontalVerticalSpacing = 8

    BrainwalletScaffold(
        topBar = {
            BrainwalletTopAppBar(
                navigationIcon = {
                    /// No-op Retained spacing for remote banner
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

            Image(
                modifier = Modifier
                    .fillMaxWidth(0.80f)
                    .padding(horizontalVerticalSpacing.dp),
                painter = painterResource(R.drawable.bw_white_logotype),
                contentDescription = stringResource(R.string.logo),
                colorFilter = ColorFilter.tint(
                    BrainwalletTheme.colors.content,
                ),
            )
            Spacer(modifier = Modifier.weight(1f))

            if (state.isUpdatePin) {
                Text(stringResource(R.string.UpdatePin_enterCurrent))
            } else {
                Text(stringResource(R.string.Login_ltcPrice, state.formattedCurrency))
                Text(stringResource(R.string.Login_currentLtcPrice, state.iso))
            }

            // TODO
            // https://developer.android.com/develop/ui/compose/animation/customize
            //            Box(
            //                modifier = Modifier
            //                    .fillMaxWidth()
            //                    .height(100.dp)
            //                    .background(Color.White)
            //            ) {
            //                //todo
            //                Text("todo")
            //            }

            Spacer(modifier = Modifier.weight(1f))

            PasscodeIndicator(passcode = state.passcode)

            Spacer(Modifier.height(16.dp))

            PasscodeKeypad { passcodeKeypadEvent ->
                when (passcodeKeypadEvent) {
                    PasscodeKeypadEvent.OnDelete -> viewModel.onEvent(UnLockEvent.OnDeletePinDigit)
                    is PasscodeKeypadEvent.OnPressed -> viewModel.onEvent(
                        UnLockEvent.OnPinDigitChange(
                            digit = passcodeKeypadEvent.digit,
                            isValidPin = { pin ->

                                //provide old logic here, its like on the BrainwalletActivity.onUnlock
                                return@OnPinDigitChange AuthManager.getInstance()
                                    .checkAuth(pin, context).also { isValid ->
                                        if (isValid) {
                                            AuthManager.getInstance().authSuccess(context)
                                            AnalyticsManager.logCustomEvent(BRConstants._20200217_DUWB)
                                            AnalyticsManager.logCustomEvent(BRConstants._20200217_DUWB)

                                        } else {
                                            AuthManager.getInstance().authFail(context)
                                            //for now just toast
                                            Toast.makeText(
                                                context,
                                                R.string.incorrect_passcode,
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                            }
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