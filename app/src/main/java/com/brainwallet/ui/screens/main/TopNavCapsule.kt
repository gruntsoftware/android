package com.brainwallet.ui.screens.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.brainwallet.ui.theme.DesignTheme
import com.brainwallet.ui.theme.bentoDarkBorderGradient
import com.brainwallet.ui.theme.bentoDarkSurfaceGradient
import com.brainwallet.ui.theme.bentoLightBorderGradient
import com.brainwallet.ui.theme.bentoLightSurfaceGradient

/**
 * Groups top nav buttons in one capsule, with the bento surface and border the buttons used to
 * draw individually. The buttons inside are borderless circles, so each tap ripple stays round.
 */
@Composable
fun TopNavCapsule(
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = false,
    content: @Composable RowScope.() -> Unit
) {
    // CircleShape on a non-square box gives fully rounded ends: a capsule.
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(CircleShape)
            .background(
                brush = if (isDarkMode) bentoDarkSurfaceGradient else bentoLightSurfaceGradient,
                shape = CircleShape
            )
            .border(
                width = 1.5.dp,
                brush = if (isDarkMode) bentoDarkBorderGradient else bentoLightBorderGradient,
                shape = CircleShape
            ),
        content = content
    )
}

@Composable
@PreviewLightDark
fun TopNavCapsulePreview() {
    val isDarkMode = isSystemInDarkTheme()
    DesignTheme(isDarkMode) {
        TopNavCapsule(isDarkMode = isDarkMode) {
            ThemeButton(isDarkMode = isDarkMode) {}
            LockButton(isDarkMode = isDarkMode) {}
        }
    }
}
