package com.brainwallet.ui.screen.unlock.composable

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CircleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.filledIconButtonColors(
        containerColor = Color.White.copy(alpha = 0.1f)
    ),
    content: @Composable () -> Unit
) {
    FilledIconButton(
        onClick = onClick,
        modifier = modifier.size(72.dp),
        enabled = enabled,
        shape = CircleShape,
        colors = colors,
        content = content
    )
}