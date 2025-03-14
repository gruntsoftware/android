package com.brainwallet.ui.screens.home.composable.settingsrows

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.brainwallet.ui.composable.DarkModeToggleButton
import com.brainwallet.ui.screens.home.SettingsEvent
import com.brainwallet.ui.screens.home.SettingsViewModel
import com.brainwallet.ui.screens.home.composable.RowActionType
import com.brainwallet.ui.theme.BrainwalletTheme
import org.koin.compose.koinInject

@Deprecated(message = "plesase use SettingRowItem instead, will remove it later")
@Composable
fun SettingsSimpleRowItem(
    modifier: Modifier = Modifier,
    mainLabel: String,
    detailLabel: String,
    actionType: RowActionType,
    viewModel: SettingsViewModel = koinInject()
) {

    val state by viewModel.state.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    /// Layout values
    val contentHeight = 60
    val horizontalPadding = 14
    val dividerThickness = 1
    val iconButtonSize = 22
    val toggleButtonSize = 24


    Column(
        modifier = modifier
    ) {
        HorizontalDivider(thickness = dividerThickness.dp, color = BrainwalletTheme.colors.content)

        Row(
            modifier = Modifier
                .height(contentHeight.dp)
                .padding(horizontal = horizontalPadding.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = mainLabel,
                style = MaterialTheme.typography.labelLarge
                    .copy(textAlign = TextAlign.Left)
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = detailLabel,
                style = MaterialTheme.typography.labelSmall
                    .copy(textAlign = TextAlign.Right)
            )

            if (actionType == RowActionType.THEME_TOGGLE) {
                DarkModeToggleButton(
                    modifier = Modifier
                        .width(toggleButtonSize.dp)
                        .aspectRatio(1f),
                    checked = state.darkMode,
                    onCheckedChange = {
                        viewModel.onEvent(SettingsEvent.OnToggleDarkMode)
                        //todo
                    }
                )
            }

            if (actionType == RowActionType.LOCK_TOGGLE) {
                DarkModeToggleButton(
                    modifier = Modifier
                        .width(toggleButtonSize.dp)
                        .aspectRatio(1f),
                    checked = state.darkMode,
                    onCheckedChange = {
                        viewModel.onEvent(SettingsEvent.OnToggleLock)
                    }
                )
            }

        }
    }

}