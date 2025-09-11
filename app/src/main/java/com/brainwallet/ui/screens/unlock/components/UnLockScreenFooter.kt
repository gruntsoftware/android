package com.brainwallet.ui.screens.unlock.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.brainwallet.R
import com.brainwallet.data.model.AppSetting
import com.brainwallet.ui.composable.DarkModeToggleButton
import com.brainwallet.ui.screens.unlock.UnLockEvent
import com.brainwallet.ui.theme.BrainwalletAppTheme
import com.brainwallet.ui.theme.BrainwalletTheme
import com.brainwallet.ui.theme.LocalDarkModeFlag

@Composable
fun UnLockScreenFooter(
    version: String,
    modifier: Modifier = Modifier,
    onEvent: (UnLockEvent) -> Unit = {}
) {
    Column(
        modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .padding(bottom = 24.dp)
                .padding(horizontal = 85.dp)
        ) {
            DarkModeToggleButton(
                checked = LocalDarkModeFlag.current,
                onCheckedChange = {
                    onEvent(UnLockEvent.OnToggleDarkMode)
                },
                iconButtonSizeInDp = 43
            )
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painterResource(R.drawable.ic_clickable_qr),
                contentDescription = "clickable_qr",
                modifier = Modifier.size(39.dp).clickable {
                    onEvent(UnLockEvent.OnQrClicked)
                },
                colorFilter = ColorFilter.tint(
                    BrainwalletTheme.colors.border
                )
            )
        }
        Text(
            version,
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography
                .bodyMedium
                .copy(
                    textAlign = TextAlign.Center,
                    color = BrainwalletTheme.colors.border
                )
        )
    }
}

@PreviewLightDark
@Composable
private fun UnLockScreenFooterPreview() {
    BrainwalletAppTheme(AppSetting(isDarkMode = isSystemInDarkTheme())) {
        Box(
            modifier = Modifier
                .background(BrainwalletTheme.colors.background)
                .fillMaxWidth()
        ) {
            UnLockScreenFooter(version = "v4.0.0 (202501201)")
        }
    }
}
