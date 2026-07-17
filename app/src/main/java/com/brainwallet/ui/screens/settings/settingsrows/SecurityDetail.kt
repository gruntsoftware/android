package com.brainwallet.ui.screens.settings.settingsrows

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.R
import com.brainwallet.ui.screens.settings.SettingsEvent

// TODO
@Composable
fun SecurityDetail(
    modifier: Modifier = Modifier,
    shareAnalyticsDataEnabled: Boolean = false,
    userSetEmojis: Boolean = false,
    onEvent: (SettingsEvent) -> Unit
) {
    // / Layout values
    val contentHeight = 60
    val horizontalPadding = 14

    SettingRowItemExpandable(
        modifier = modifier,
        title = stringResource(R.string.settings_title_security),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = horizontalPadding.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .height(contentHeight.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.security_PIN_title))
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = {
                    onEvent.invoke(SettingsEvent.OnSecurityUpdatePinClick)
                }) {
                    Text(stringResource(R.string.security_PIN_button))
                }
            }

            Row(
                modifier = Modifier
                    .height(contentHeight.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.security_seed_phrase_title))
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = {
                    onEvent.invoke(SettingsEvent.OnSecuritySeedPhraseClick)
                }) {
                    Text(stringResource(R.string.security_phrase_button))
                }
            }

            if (userSetEmojis) {
                Row(
                    modifier = Modifier
                        .height(contentHeight.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.security_brainwallet_phrase_title))
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = {
                        onEvent.invoke(SettingsEvent.OnSecurityBrainwalletPhraseClick)
                    }) {
                        Text(
                            text = "😅",
                            style = TextStyle(
                                fontWeight = FontWeight.Normal,
                                fontSize = 20.sp,
                                shadow = Shadow(
                                    color = Color.Black.copy(alpha = 0.5f),
                                    offset = Offset(x = 4f, y = 4f),
                                    blurRadius = 4f
                                )
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .height(contentHeight.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.security_share_data_title))
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = {
                    onEvent.invoke(SettingsEvent.OnSecurityShareAnalyticsDataClick)
                }) {
                    Text(stringResource(if (shareAnalyticsDataEnabled) R.string.Button_yes else R.string.Button_no))
                }
            }
        }
    }
}
