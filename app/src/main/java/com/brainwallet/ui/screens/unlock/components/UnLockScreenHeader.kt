package com.brainwallet.ui.screens.unlock.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.R
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.data.model.AppSetting
import com.brainwallet.ui.theme.BrainwalletAppTheme
import com.brainwallet.ui.theme.DesignTheme
import com.brainwallet.R.drawable
import com.brainwallet.ui.theme.IBMPlexSans
import com.brainwallet.ui.theme.LocalIsDarkModeFlag

@Composable
fun UnLockScreenHeader(
    formattedLtcPrice: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val iconLogo = if (LocalIsDarkModeFlag.current) {
            com.brainwallet.R.drawable.brainwallet_logotype_white
        } else {
            com.brainwallet.R.drawable.brainwallet_logotype_color
        }

        Text(
            modifier = Modifier
                .padding(
                    top = 40.dp,
                    bottom = 20.dp
                )
                .fillMaxWidth(),
            text = formattedLtcPrice,
            style = TextStyle(
                fontFamily = IBMPlexSans,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            ),
            textAlign = TextAlign.End,
            color = DesignTheme.colors.border
        )
        Image(
            painterResource(
                iconLogo
            ),
            contentDescription = "brainwallet_logo",
            modifier = Modifier.width(268.dp)
        )
    }
}

@PreviewLightDark
@Composable
private fun UnLockScreenHeaderPreview() {
    BrainwalletAppTheme(appSetting = AppSetting(isDarkMode = isSystemInDarkTheme())) {
        Box(modifier = Modifier.background(DesignTheme.colors.background)) {
            UnLockScreenHeader(formattedLtcPrice = "100")
        }
    }
}
