@file:OptIn(ExperimentalLayoutApi::class)

package com.brainwallet.ui.composable.passcode

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Face
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

@Composable
fun PasscodeKeypad(
    onEvent: (PasscodeKeypadEvent) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
        maxItemsInEachRow = 3
    ) {
        // pin number button
        val modifierCircleButton = Modifier.size(60.dp)

        repeat(9) { index ->
            val number = index + 1
            _root_ide_package_.com.brainwallet.ui.composable.CircleButton(
                modifier = modifierCircleButton.testTag("keypad$number"),
                onClick = {
                    onEvent.invoke(PasscodeKeypadEvent.OnPressed(number))
                },
            ) {
                Text(
                    text = number.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
        }

        // Bottom row with biometric, 0, and backspace
        if (false) { // hardcoded false for now
            _root_ide_package_.com.brainwallet.ui.composable.CircleButton(
                modifier = modifierCircleButton,
                onClick = {
                    //
                },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color.Transparent
                ),
            ) {
                Icon(
                    Icons.Default.Face,
                    contentDescription = "Biometric",
                )
            }
        } else {
            _root_ide_package_.com.brainwallet.ui.composable.CircleButton(
                modifier = modifierCircleButton,
                onClick = { },
                colors = IconButtonDefaults.filledIconButtonColors(
                    disabledContainerColor = Color.Transparent
                ),
                enabled = false,
            ) {
                Spacer(modifier = Modifier)
            }
        }

        _root_ide_package_.com.brainwallet.ui.composable.CircleButton(
            modifier = modifierCircleButton,
            onClick = {
                onEvent.invoke(PasscodeKeypadEvent.OnPressed(0))
            },
        ) {
            Text(
                text = "0",
                style = MaterialTheme.typography.headlineMedium,
            )
        }

        _root_ide_package_.com.brainwallet.ui.composable.CircleButton(
            modifier = modifierCircleButton,
            onClick = {
                onEvent.invoke(PasscodeKeypadEvent.OnDelete)
            },
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color.Transparent
            ),
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Delete",
            )
        }
    }
}
