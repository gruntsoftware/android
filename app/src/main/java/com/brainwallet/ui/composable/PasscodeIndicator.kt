package com.brainwallet.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun PasscodeIndicator(
    passcode: List<Int>
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        passcode.forEach { digit ->
            PinDotItem(checked = digit > -1)
        }
    }
}