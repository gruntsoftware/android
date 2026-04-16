package com.brainwallet.ui.composable

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

@Composable
fun PinKeyboard(
    onDigitClick: (Int) -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // First row: 1, 2, 3
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            repeat(3) { index ->
                val number = index + 1
                PinKeyboardButton(
                    onClick = { onDigitClick(number) },
                    modifier = Modifier.testTag("keypad$number")
                ) {
                    Text(
                        text = number.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
            }
        }

        // Second row: 4, 5, 6
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            repeat(3) { index ->
                val number = index + 4
                PinKeyboardButton(
                    onClick = { onDigitClick(number) },
                    modifier = Modifier.testTag("keypad$number")
                ) {
                    Text(
                        text = number.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
            }
        }

        // Third row: 7, 8, 9
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            repeat(3) { index ->
                val number = index + 7
                PinKeyboardButton(
                    onClick = { onDigitClick(number) },
                    modifier = Modifier.testTag("keypad$number")
                ) {
                    Text(
                        text = number.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
            }
        }

        // Fourth row: empty, 0, delete
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Empty space
            Spacer(modifier = Modifier.size(75.dp))

            // Zero button
            PinKeyboardButton(
                onClick = { onDigitClick(0) },
                modifier = Modifier.testTag("keypad0")
            ) {
                Text(
                    text = "0",
                    style = MaterialTheme.typography.headlineMedium,
                )
            }

            // Delete button
            PinKeyboardButton(
                onClick = onDeleteClick,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color.Transparent
                )
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Delete",
                    tint = BrainwalletTheme.colors.content
                )
            }
        }
    }
}

@Composable
private fun PinKeyboardButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: androidx.compose.material3.IconButtonColors = IconButtonDefaults.filledIconButtonColors(),
    content: @Composable () -> Unit
) {
    CircleButton(
        modifier = modifier.size(75.dp),
        onClick = onClick,
        colors = colors,
        content = content
    )
}

@Preview(name = "Light Theme")
@Preview(name = "Dark Theme", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PinKeyboardPreview() {
    BrainwalletTheme(darkTheme = false) {
        PinKeyboard(
            onDigitClick = { },
            onDeleteClick = { }
        )
    }
}
