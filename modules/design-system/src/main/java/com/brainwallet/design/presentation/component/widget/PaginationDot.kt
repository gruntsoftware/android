package com.brainwallet.design.presentation.component.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

@Composable
fun PaginationDot(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val dotColor = if (isActive) {
        BrainwalletTheme.colors.info
    } else {
        BrainwalletTheme.colors.content.copy(alpha = 0.3f)
    }

    Box(
        modifier = modifier
            .size(6.dp)
            .clip(CircleShape)
            .background(dotColor)
    )
}

@PreviewLightDark
@Preview
@Composable
private fun GridChipPreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        PaginationDot(isActive = true)
    }
}
