@file:Suppress("UNREACHABLE_CODE")

package com.brainwallet.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.brainwallet.ui.theme.BrainwalletTheme
import com.brainwallet.ui.theme.chilli

@Composable
fun SeedWordItem(
    modifier: Modifier = Modifier,
    label: String,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
) {
    SeedWordItemBox(modifier = modifier) {
        Text(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .weight(1f),
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isError) chilli else BrainwalletTheme.colors.content,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
        trailingIcon?.let { icon ->
            Spacer(modifier = Modifier.width(8.dp))
            icon()
        }
    }
}

@Composable
fun SeedWordItemBox(
    modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit
) {
    Box(modifier = modifier) {
        Row(
            modifier = modifier
                .background(
                    color = BrainwalletTheme.colors.background.copy(alpha = 0.3f),
                    shape = MaterialTheme.shapes.extraLarge
                )
                .padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            content.invoke(this)
        }
    }
}

@Composable
fun SeedWordItemTextField(
    value: String,
    onValueChange: (String) -> Unit,
    suggestions: List<String> = emptyList(),
    modifier: Modifier = Modifier,
    textStyle: TextStyle = TextStyle.Default.copy(
        color = BrainwalletTheme.colors.content
    ),
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    cursorBrush: Brush = SolidColor(BrainwalletTheme.colors.content), //todo: change with materialtheme so it will be adapt automatically when switch darkmode
    prefix: @Composable (() -> Unit)? = null,
) {
    var suggestionsExpanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    LaunchedEffect(isFocused) {
        if (!isFocused) {
            suggestionsExpanded = false // Dismiss dropdown when focus is lost
        }
    }

    Box(modifier = modifier) {
        BasicTextField(
            modifier = Modifier.focusable(interactionSource = interactionSource),
            value = value,
            textStyle = textStyle,
            cursorBrush = cursorBrush,
            onValueChange = { newValue ->
                onValueChange.invoke(newValue)
                suggestionsExpanded = true
            },
            singleLine = true,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            interactionSource = interactionSource,
            decorationBox = { innerTextField ->
                SeedWordItemBox {
                    Box(
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Bottom

                        ) {
                            prefix?.invoke()
                            innerTextField.invoke()
                        }
                    }
                }
            })

        DropdownMenu(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .background(
                    color = BrainwalletTheme.colors.background.copy(alpha = 0.3f),
                    shape = MaterialTheme.shapes.medium
                )
                .heightIn(max = 250.dp),
            properties = PopupProperties(focusable = false),
            offset = DpOffset(8.dp, 0.dp),
            expanded = suggestionsExpanded && suggestions.isNotEmpty(),
            onDismissRequest = { suggestionsExpanded = false },
        ) {
            suggestions.forEach { suggestion ->
                DropdownMenuItem(text = {
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }, onClick = {
                    onValueChange(suggestion)
                    suggestionsExpanded = false
                })
            }
        }

    }

}


///