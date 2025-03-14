package com.brainwallet.ui.screens.home.composable.settingsrows

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.brainwallet.R
import com.brainwallet.ui.screens.home.SettingsViewModel
import org.koin.compose.koinInject

//TODO
@Composable
fun LitecoinBlockchainDetail(
    modifier: Modifier = Modifier,
    onUserDidStartSync: (Boolean) -> Unit,
) {
    /// Layout values
    val closedHeight = 60
    val expandedHeight = 100
    val toggleButtonSize = 40
    val dividerThickness = 1
    val iconButtonSize = 22
    val horizontalPadding = 14

    SettingRowItemExpandable(
        modifier = modifier,
        title = stringResource(R.string.blockchain_litecoin)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = horizontalPadding.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Todo Content")
            Button(onClick = {}) {
                Text(stringResource(R.string.ReScan_alertAction))
            }
        }
    }
}