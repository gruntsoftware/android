package com.brainwallet.ui.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.brainwallet.ui.theme.DesignTheme
import com.brainwallet.R

@Composable
fun BentoSettingsButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.size(48.dp),
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = DesignTheme.colors.surface),
        border = BorderStroke(1.dp, DesignTheme.colors.content.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_burger_menu),
                contentDescription = "Menu",
                tint = DesignTheme.colors.content
            )
        }
    }
}

@Composable
@PreviewLightDark
fun BentoSettingsButtonPreview() {
    DesignTheme(isSystemInDarkTheme()) {
        BentoSettingsButton {}
    }
}
