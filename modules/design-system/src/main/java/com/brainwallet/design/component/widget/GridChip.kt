package com.brainwallet.design.component.widget

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

private const val GRID_ALPHA = 0.4f

@Composable
fun GridChip(text: String, modifier: Modifier = Modifier) {
    Card(
        modifier,
        colors = CardDefaults.cardColors(
            containerColor = BrainwalletTheme.colors.affirm.copy(alpha = GRID_ALPHA),
            contentColor = BrainwalletTheme.colors.content
        )
    ) {
        Text(
            text,
            style = BrainwalletTheme.typography.labelMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@PreviewLightDark
@Preview
@Composable
private fun GridChipPreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        GridChip("Preview")
    }
}
