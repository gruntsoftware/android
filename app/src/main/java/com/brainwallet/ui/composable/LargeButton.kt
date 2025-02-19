package com.brainwallet.ui.composable

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.brainwallet.ui.theme.BrainwalletColors
import com.brainwallet.ui.theme.BrainwalletTheme

@Composable
fun LargeButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = BrainwalletTheme.colors.background,
        contentColor = BrainwalletTheme.colors.content
    ),
    shape: Shape = ButtonDefaults.shape,
    content: @Composable RowScope.() -> Unit
) {
    FilledTonalButton(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        onClick = onClick,
        colors = colors,
        shape = shape,
        content = content
    )
}

@Composable
fun BorderedLargeButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = BrainwalletTheme.colors.background,
        contentColor = BrainwalletTheme.colors.content
    ),
    shape: Shape = RoundedCornerShape(50),
    content: @Composable RowScope.() -> Unit
) {
    LargeButton(
        modifier = modifier.border(
            2.dp,
            colors.contentColor,
            shape
        ),
        onClick = onClick,
        colors = colors,
        shape = shape,
        content = content
    )
}