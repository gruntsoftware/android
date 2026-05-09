package com.brainwallet.ui.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.brainwallet.ui.theme.lavender
import com.brainwallet.ui.theme.midnight

@Composable
fun BrainwalletLoadingIndicator(
    modifier: Modifier = Modifier,
    color: Color = BrainwalletTheme.colors.content
) {
    CircularProgressIndicator(
        color = lavender,
        trackColor = midnight,
        strokeWidth = 5.dp,
        modifier = Modifier.size(48.dp).then(modifier)
    )
}

@PreviewLightDark
@Composable
private fun BrainwalletLoadingIndicatorPreview() {
    Box(modifier = Modifier) {
        BrainwalletLoadingIndicator()
    }
}
