package com.brainwallet.ui.screens.home.composable.settingsrows

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.brainwallet.ui.theme.BrainwalletTheme

//TODO
@Composable
fun SettingsSimpleRowItem(
    modifier: Modifier = Modifier,
    mainLabel: String,
    detailLabel: String
) {
    var expanded by remember { mutableStateOf(false) }
    /// Layout values
    val contentHeight = 60
    val horizontalPadding = 14
    val dividerThickness = 1

    Column(
        modifier = modifier
    ) {
        HorizontalDivider(thickness = dividerThickness.dp, color = BrainwalletTheme.colors.content)

        Row (
                modifier = Modifier
                    .height(contentHeight.dp)
                    .padding(horizontal = horizontalPadding.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                Text(text = mainLabel,
                    style = MaterialTheme.typography.labelLarge
                    .copy(textAlign = TextAlign.Left)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(text = detailLabel,
                        style = MaterialTheme.typography.labelSmall
                            .copy(textAlign = TextAlign.Right)
                )
        }
    }

}
