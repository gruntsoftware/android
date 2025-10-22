package com.brainwallet.design.component.widget

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
import com.brainwallet.design.component.effect.CardGlassContainer
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

@Composable
fun BentoHomeGrid(
    name: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    CardGlassContainer(
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
                text = name,
                style = BrainwalletTheme.typography.bodySmall.copy(
                    color = BrainwalletTheme.colors.content,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
@PreviewLightDark
fun BentoHomeGridPreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        BentoHomeGrid("Test")
    }
}
