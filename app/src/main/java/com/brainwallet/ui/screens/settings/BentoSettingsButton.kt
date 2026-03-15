package com.brainwallet.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.brainwallet.ui.theme.DesignTheme
import com.brainwallet.R
import com.brainwallet.ui.theme.bentoDarkBorderGradient
import com.brainwallet.ui.theme.bentoDarkSurfaceGradient
import com.brainwallet.ui.theme.bentoLightBorderGradient
import com.brainwallet.ui.theme.bentoLightSurfaceGradient

@Composable
fun BentoSettingsButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(48.dp)
            .clip(CircleShape)
            .background(
                brush = if (isSystemInDarkTheme()) bentoDarkSurfaceGradient else bentoLightSurfaceGradient,
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                brush = if (isSystemInDarkTheme()) bentoDarkBorderGradient else bentoLightBorderGradient,
                shape = CircleShape
            )
            .clickable(onClick = onClick)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_burger_menu),
            contentDescription = "Menu",
            tint = DesignTheme.colors.content,
        )
    }
}

@Composable
@PreviewLightDark
fun BentoSettingsButtonPreview() {
    DesignTheme(isSystemInDarkTheme()) {
        BentoSettingsButton {}
    }
}
