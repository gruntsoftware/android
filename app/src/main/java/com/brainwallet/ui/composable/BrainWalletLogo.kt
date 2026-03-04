package com.brainwallet.ui.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.brainwallet.R
import com.brainwallet.data.model.AppSetting
import com.brainwallet.ui.theme.BrainwalletAppTheme
import com.brainwallet.ui.theme.DesignTheme
import com.brainwallet.ui.theme.LocalIsDarkModeFlag

@Composable
fun BrainWalletLogo(modifier: Modifier = Modifier) {
    val iconLogo = if (LocalIsDarkModeFlag.current) {
        R.drawable.brainwallet_logotype_white
    } else {
        R.drawable.brainwallet_logotype_color
    }
    Image(
        painterResource(iconLogo),
        contentDescription = "brainwallet_logo",
        modifier = modifier
    )
}

@PreviewLightDark
@Composable
private fun BrainWalletLogoPreview() {
    BrainwalletAppTheme(appSetting = AppSetting(isDarkMode = isSystemInDarkTheme())) {
        Box(modifier = Modifier.background(DesignTheme.colors.background)) {
            BrainWalletLogo()
        }
    }
}
