package com.brainwallet.ui.screens.home.composable.settingsrows

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.brainwallet.R
import com.brainwallet.ui.composable.DarkModeToggleButton

@Composable
fun ThemeSettingRowItem(
    darkMode: Boolean = true,
    onToggledDarkMode: () -> Unit
) {
    val toggleButtonSize = 24
    SettingRowItem(
        title = stringResource(R.string.settings_title_theme),
        onClick = onToggledDarkMode
    ) {
        DarkModeToggleButton(
            modifier = Modifier
                .width(toggleButtonSize.dp)
                .aspectRatio(1f),
            checked = darkMode,
            onCheckedChange = {
                onToggledDarkMode.invoke()
            }
        )
    }
}