package com.brainwallet.ui.screens.unlock.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.brainwallet.R
import com.brainwallet.data.model.AppSetting
import com.brainwallet.tools.manager.AnalyticsManager
import com.brainwallet.tools.security.AuthManager
import com.brainwallet.ui.composable.PasscodeIndicator
import com.brainwallet.ui.composable.PasscodeKeypad
import com.brainwallet.ui.composable.PasscodeKeypadEvent
import com.brainwallet.constants.BWConstants
import com.brainwallet.ui.screens.unlock.UnLockEvent
import com.brainwallet.ui.theme.BrainwalletAppTheme
import com.brainwallet.ui.theme.DesignTheme
import com.google.common.collect.ImmutableList

@Composable
fun UnLockScreenBody(
    passcode: List<Int>,
    isUpdatePin: Boolean,
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Center,
    onEvent: (UnLockEvent) -> Unit = {}
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .padding(18.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = verticalArrangement,
    ) {
        AnimatedVisibility(isUpdatePin) {
            Text(
                stringResource(R.string.UpdatePin_enterCurrent),
                modifier = Modifier
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        PasscodeIndicator(passcode = passcode, modifier = Modifier)
        Spacer(modifier = Modifier.weight(1f))
        PasscodeKeypad { passcodeKeypadEvent ->
            when (passcodeKeypadEvent) {
                PasscodeKeypadEvent.OnDelete -> onEvent(UnLockEvent.OnDeletePinDigit)
                is PasscodeKeypadEvent.OnPressed -> onEvent(
                    UnLockEvent.OnPinDigitChange(
                        digit = passcodeKeypadEvent.digit,
                        isValidPin = { pin ->

                            // provide old logic here, its like on the BrainwalletActivity.onUnlock
                            return@OnPinDigitChange AuthManager.getInstance()
                                .checkAuth(pin, context).also { isValid ->
                                    if (isValid) {
                                        AuthManager.getInstance().authSuccess(context)
                                        AnalyticsManager.logCustomEvent(BWConstants._20200217_DUWB)
                                    } else {
                                        // Auth Fail toast
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
    }
}

@PreviewLightDark
@Composable
private fun UnLockScreenBodyPreview() {
    BrainwalletAppTheme(AppSetting(isDarkMode = isSystemInDarkTheme())) {
        Box(
            modifier = Modifier
                .background(DesignTheme.colors.background)
                .fillMaxWidth()
        ) {
            UnLockScreenBody(isUpdatePin = true, passcode = ImmutableList.of(1, 2))
        }
    }
}
