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
 * Learning banner component for the navigation rail.
 * Promotes educational content with glass effect styling.
 */
@Composable
fun BentoRailLearningBanner(
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
                "Learning Banner",
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
fun BentoRailLearningBannerPreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        BentoRailLearningBanner()
    }
}
