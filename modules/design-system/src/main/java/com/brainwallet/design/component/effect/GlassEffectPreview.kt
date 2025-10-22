package com.brainwallet.design.component.effect

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

/**
 * Showcase composable demonstrating different glass effect variations.
 */
@Composable
fun GlassEffectShowcase(
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedLightBleedBackground(
            modifier = Modifier.fillMaxSize(),
            bleedIntensity = 0.12f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LightGlassContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                GlassEffectCard("Light Glass Container")
            }

            MediumGlassContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                GlassEffectCard("Medium Glass Container")
            }

            HeavyGlassContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                GlassEffectCard("Heavy Glass Container")
            }

            CardGlassContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                GlassEffectCard("Card Glass Container")
            }
        }
    }
}

@Composable
private fun GlassEffectCard(
    title: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = BrainwalletTheme.typography.bodyMedium.copy(
                color = BrainwalletTheme.colors.content,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@PreviewLightDark
@Composable
fun GlassEffectShowcasePreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        GlassEffectShowcase()
    }
}
