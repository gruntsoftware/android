package com.brainwallet.ui.screens.unlock.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.brainwallet.data.model.AppSetting
import com.brainwallet.ui.composable.BrainWalletLogo
import com.brainwallet.ui.theme.BrainwalletAppTheme
import com.brainwallet.ui.theme.BrainwalletTheme

@Composable
fun UnLockScreenHeader(
    formattedLtcPrice: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            modifier = Modifier
                .padding(bottom = 52.dp)
                .fillMaxWidth(),
            text = formattedLtcPrice,
            textAlign = TextAlign.End,
            color = BrainwalletTheme.colors.border
        )
        BrainWalletLogo(modifier = Modifier.width(268.dp))
    }
}

@PreviewLightDark
@Composable
private fun UnLockScreenHeaderPreview() {
    BrainwalletAppTheme(appSetting = AppSetting(isDarkMode = isSystemInDarkTheme())) {
        Box(modifier = Modifier.background(BrainwalletTheme.colors.background)) {
            UnLockScreenHeader(formattedLtcPrice = "100")
        }
    }
}
