package com.brainwallet.ui.screens.home.composable.settingsrows

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.brainwallet.R

@Composable
fun LockSettingRowItem(
    onClick: () -> Unit,
) {
    SettingRowItem(
        title = stringResource(R.string.settings_title_lock),
        onClick = onClick
    ) {
        Icon(Icons.Default.Lock, contentDescription = stringResource(R.string.settings_title_lock))
    }
}