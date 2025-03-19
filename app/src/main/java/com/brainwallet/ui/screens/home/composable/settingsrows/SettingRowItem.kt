package com.brainwallet.ui.screens.home.composable.settingsrows

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.brainwallet.ui.theme.BrainwalletColors
import com.brainwallet.ui.theme.BrainwalletTheme

/**
 * main composable for [SettingRowItem] and [SettingRowItemExpandable]
 */

@Composable
fun SettingRowItem(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    onClick: (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    SettingRowItemExpandable(
        modifier = modifier,
        title = title,
        description = description,
        trailingIcon = trailingIcon,
        hasExpandedContent = false,
        onClick = onClick
    ) { }
}

@Composable
fun SettingRowItemExpandable(
    modifier: Modifier = Modifier,
    title: String,
    description: String? = null,
    hasExpandedContent: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    colors: ListItemColors = ListItemDefaults.colors(
        containerColor = BrainwalletTheme.colors.surface,
        headlineColor = BrainwalletTheme.colors.content,
        supportingColor = BrainwalletTheme.colors.content.copy(0.6f),
        trailingIconColor = BrainwalletTheme.colors.content,
    ),
    content: @Composable () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    /// Layout values
    val closedHeight = 60
    val expandedHeight = 100
    val dividerThickness = 1
    val horizontalPadding = 14


    Column(
        modifier = modifier.background(BrainwalletTheme.colors.settingRowItemBackground(expanded))
    ) {
        HorizontalDivider(thickness = dividerThickness.dp, color = BrainwalletTheme.colors.content)
        ListItem(
            colors = colors,
            modifier = Modifier.clickable {
                if (hasExpandedContent) {
                    expanded = expanded.not()
                } else {
                    onClick?.invoke()
                }
            },
            headlineContent = {
                Text(
                    text = title,
                    style = BrainwalletTheme.typography.labelLarge
                        .copy(textAlign = TextAlign.Left)
                )
            },
            supportingContent = {
                description?.let { Text(text = it) }
            },
            trailingContent = {
                if (hasExpandedContent) {
                    Icon(
                        if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                } else {
                    trailingIcon?.invoke()
                }
            }
        )

        AnimatedVisibility(
            visible = expanded,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            content.invoke()
        }
    }
}

fun BrainwalletColors.settingRowItemBackground(
    expanded: Boolean = false
) = if (expanded) background else surface