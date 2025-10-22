package com.brainwallet.design.component.widget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.brainwallet.design.R
import com.brainwallet.design.component.effect.LightOpacityContainer
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

@Composable
fun BentoRailButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    LightOpacityContainer(
        modifier = modifier
            .size(48.dp)
            .clickable { onClick() },
        shape = CircleShape
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_burger_menu),
                contentDescription = "Menu",
                tint = BrainwalletTheme.colors.content
            )
        }
    }
}

@Composable
@PreviewLightDark
fun BentoRailButtonPreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        BentoRailButton {}
    }
}
