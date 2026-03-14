package com.brainwallet.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.brainwallet.R
import com.brainwallet.data.model.AppSetting
import com.brainwallet.ui.theme.BrainwalletAppTheme
import com.brainwallet.ui.theme.DesignTheme

@Composable
fun DarkModeToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    iconButtonSizeInDp: Int = 32
) {
    IconToggleButton(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier.size(iconButtonSizeInDp.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .size(iconButtonSizeInDp.dp)
                .aspectRatio(1f)
                .clip(CircleShape)
                .border(
                    1.dp,
                    if (checked) BrainwalletTheme.colors.warn else DesignTheme.colors.surface,
                    CircleShape
                )
                .background(if (checked) DesignTheme.colors.surface else DesignTheme.colors.content)
        ) {
            Icon(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size((iconButtonSizeInDp * 0.6).dp),
                tint = if (checked) BrainwalletTheme.colors.warn else DesignTheme.colors.surface,
                painter = painterResource(if (checked) R.drawable.ic_light_mode else R.drawable.ic_dark_mode),
                contentDescription = stringResource(R.string.toggle_dark_mode),
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun DarkModeToggleButtonPreview() {
    BrainwalletAppTheme(AppSetting(isDarkMode = isSystemInDarkTheme())) {
        Box(modifier = Modifier.background(DesignTheme.colors.background)) {
            DarkModeToggleButton(checked = isSystemInDarkTheme(), onCheckedChange = {},)
        }
    }
}
