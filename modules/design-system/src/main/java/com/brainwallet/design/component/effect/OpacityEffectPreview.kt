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
 * Showcase composable demonstrating different opacity effect variations.
 */
@Composable
fun OpacityEffectShowcase(
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
            LightOpacityContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                OpacityEffectCard("Light Opacity Container")
            }

            MediumOpacityContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                OpacityEffectCard("Medium Opacity Container")
            }

            HeavyOpacityContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                OpacityEffectCard("Heavy Opacity Container")
            }

            CardOpacityContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
            ) {
                OpacityEffectCard("Card Opacity Container")
            }
        }
    }
}

@Composable
private fun OpacityEffectCard(
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
fun OpacityEffectShowcasePreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        OpacityEffectShowcase()
    }
}
