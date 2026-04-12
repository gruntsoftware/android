package com.brainwallet.ui.composable

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

@Composable
fun PinDot(
    isFilled: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(32.dp)
            .background(
                color = if (isFilled) {
                    BrainwalletTheme.colors.content
                } else {
                    Color.Transparent
                },
                shape = CircleShape
            )
            .border(
                width = 2.dp,
                color = BrainwalletTheme.colors.content.copy(alpha = 0.6f),
                shape = CircleShape
            )
    )
}

@Preview(name = "Light Theme - Empty")
@Preview(name = "Dark Theme - Empty", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PinDotEmptyPreview() {
    BrainwalletTheme(darkTheme = false) {
        PinDot(isFilled = false)
    }
}

@Preview(name = "Light Theme - Filled")
@Preview(name = "Dark Theme - Filled", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PinDotFilledPreview() {
    BrainwalletTheme(darkTheme = false) {
        PinDot(isFilled = true)
    }
}
