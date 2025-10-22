package com.brainwallet.ui.composable

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

@Composable
fun PinInputComponent(
    pinState: PinInputState,
    onPinComplete: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxLength: Int = 4
) {
    LaunchedEffect(pinState.isComplete) {
        if (pinState.isComplete) {
            onPinComplete(pinState.getPinAsString())
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // PIN dots display
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(maxLength) { index ->
                PinDot(
                    isFilled = index < pinState.pinLength
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // PIN keyboard
        PinKeyboard(
            onDigitClick = { digit ->
                pinState.addDigit(digit)
            },
            onDeleteClick = {
                pinState.deleteDigit()
            }
        )
    }
}

@Composable
fun PinInputComponent(
    onPinComplete: (String) -> Unit,
    modifier: Modifier = Modifier,
    maxLength: Int = 4
) {
    val pinState = rememberPinInputState(maxLength = maxLength)

    PinInputComponent(
        pinState = pinState,
        onPinComplete = onPinComplete,
        modifier = modifier,
        maxLength = maxLength
    )
}

@Preview(name = "Light Theme - Empty")
@Preview(name = "Dark Theme - Empty", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PinInputComponentEmptyPreview() {
    BrainwalletTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            val pinState = rememberPinInputState()
            PinInputComponent(
                pinState = pinState,
                onPinComplete = { }
            )
        }
    }
}

@Preview(name = "Light Theme - Partial")
@Preview(name = "Dark Theme - Partial", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PinInputComponentPartialPreview() {
    BrainwalletTheme(darkTheme = false) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            val pinState = rememberPinInputState()
            // Simulate partial input
            pinState.addDigit(1)
            pinState.addDigit(2)

            PinInputComponent(
                pinState = pinState,
                onPinComplete = { }
            )
        }
    }
}
