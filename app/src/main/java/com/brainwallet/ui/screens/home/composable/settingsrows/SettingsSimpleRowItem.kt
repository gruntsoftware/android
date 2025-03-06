package com.brainwallet.ui.screens.home.composable.settingsrows

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.brainwallet.ui.theme.BrainwalletTheme

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MenuDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp

//TODO
@Composable
fun SettingsSimpleRowItem(
    modifier: Modifier = Modifier,
    mainLabel: String,
    detailLabel: String,
    url: String?
) {
    var expanded by remember { mutableStateOf(false) }
    /// Layout values
    val contentHeight = 60
    Column(
        modifier = modifier
    ) {
        Box() {
            Row (
                modifier = Modifier.height(contentHeight.dp)
            ){
                Text(mainLabel)
                Spacer(modifier = Modifier.weight(1f))
                if(url != null) {
                    Text(detailLabel)
                }
                else {
                    Text(detailLabel)
                }
            }
        }
    }

}
