package com.brainwallet.ui.screens.home.composable.settingsrows

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.brainwallet.R

//TODO
@Composable
fun SecurityDetail(
    modifier: Modifier = Modifier
) {
    val horizontalPadding = 14

    SettingRowItemExpandable(
        modifier = modifier,
        title = stringResource(R.string.settings_title_security),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = horizontalPadding.dp)
        ) {
            Text("TODO Content Here")
        }
    }
}