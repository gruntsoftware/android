package com.brainwallet.ui.screens.home.composable.settingsrows

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.brainwallet.R
import com.brainwallet.ui.composable.DarkModeToggleButton
import com.brainwallet.ui.screens.home.SettingsEvent
import com.brainwallet.ui.screens.home.SettingsViewModel
import com.brainwallet.ui.screens.home.composable.RowActionType
import com.brainwallet.ui.theme.BrainwalletTheme
import org.koin.compose.koinInject


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
                    }
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .width(iconButtonSize.dp)
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .border(
                                1.dp,
                                if (state.darkMode) BrainwalletTheme.colors.warn else BrainwalletTheme.colors.surface,
                                CircleShape
                            )
                            .background(if (state.darkMode) BrainwalletTheme.colors.surface else BrainwalletTheme.colors.content)
                    ) {
                        Icon(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .width(iconButtonSize.dp)
                                .aspectRatio(1f),
                            tint = if (state.darkMode) BrainwalletTheme.colors.warn else BrainwalletTheme.colors.surface,
                            painter = painterResource(if (state.darkMode) R.drawable.ic_light_mode else R.drawable.ic_dark_mode),
                            contentDescription = stringResource(R.string.toggle_dark_mode),
                        )
                    }
                }
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
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .width(iconButtonSize.dp)
                            .aspectRatio(1f)
                    ) {
                        Icon(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .width(iconButtonSize.dp)
                                .aspectRatio(1f),
                            tint = BrainwalletTheme.colors.content,
                            painter = painterResource(if (state.isLocked) R.drawable.ic_lock else R.drawable.ic_unlocked),
                            contentDescription = stringResource(R.string.toggle_lock_mode),
                        )
                    }
                }
            }

        }
    }

}
//if(actionType == RowActionType.SLIDER) {
////                    Slider(
////                        state = TODO(),
////                        modifier = TODO(),
////                        enabled = TODO(),
////                        colors = TODO(),
////                        interactionSource = TODO(),
////                        thumb = TODO(),
////                        track = TODO()
////                    )
//}
//if(actionType == RowActionType.TOGGLE) {
//
//}