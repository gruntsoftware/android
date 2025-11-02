package com.brainwallet.design.presentation.component.widget

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
import com.brainwallet.design.presentation.component.effect.LightOpacityContainer
import com.brainwallet.design.presentation.state.DarkModeState
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

@Composable
fun BentoDarkModeToggle(
    darkModeState: DarkModeState,
    modifier: Modifier = Modifier,
) {
    LightOpacityContainer(
        modifier = modifier
            .size(48.dp)
            .clickable { darkModeState.toggle() },
        shape = CircleShape
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_gear),
                contentDescription = "Settings",
                tint = BrainwalletTheme.colors.content
            )
        }
    }
}

@Composable
@PreviewLightDark
fun BentoDarkModeTogglePreview() {
    val darkModeState = DarkModeState(isSystemInDarkTheme()) { !it }
    BrainwalletTheme(darkModeState.isDarkMode) {
        BentoDarkModeToggle(darkModeState = darkModeState)
    }
}
