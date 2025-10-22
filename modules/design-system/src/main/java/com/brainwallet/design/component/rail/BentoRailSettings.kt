package com.brainwallet.design.component.rail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.brainwallet.design.component.effect.MediumGlassContainer
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

/**
 * Settings section component for the navigation rail.
 * Provides access to app settings with glass effect styling.
 */
@Composable
fun BentoRailSettings(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    MediumGlassContainer(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Settings",
                style = BrainwalletTheme.typography.bodyLarge.copy(
                    color = BrainwalletTheme.colors.content,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@PreviewLightDark
@Composable
fun BentoRailSettingsPreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        BentoRailSettings()
    }
}
