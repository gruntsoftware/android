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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.brainwallet.ui.theme.DesignTheme

@Composable
fun EditSendButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    darkMode: Boolean = false,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = if (darkMode) Color.Transparent else Color.White,
        contentColor = if (darkMode) Color.White else Color.Black,

    ),
    shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable RowScope.() -> Unit
) {
    FilledTonalButton(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        onClick = onClick,
        colors = colors,
        shape = shape,
        content = content
    )
}

@Composable
fun SendContinueButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    darkMode: Boolean = false,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = if (darkMode) Color.White else Color.Black.copy(0.7f),
        contentColor = if (darkMode) Color.Black else Color.White,
    ),
    shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable RowScope.() -> Unit
) {
    FilledTonalButton(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        onClick = onClick,
        colors = colors,
        shape = shape,
        content = content
    )
}

@Composable
fun LargeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = DesignTheme.colors.background,
        contentColor = DesignTheme.colors.content
    ),
    shape: Shape = ButtonDefaults.shape,
    content: @Composable RowScope.() -> Unit
) {
    FilledTonalButton(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled,
        onClick = onClick,
        colors = colors,
        shape = shape,
        content = content
    )
}

@Composable
fun BorderedLargeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = DesignTheme.colors.surface,
        contentColor = DesignTheme.colors.content
    ),
    shape: Shape = RoundedCornerShape(50),
    content: @Composable RowScope.() -> Unit
) {
    LargeButton(
        modifier = modifier.border(
            1.dp,
            colors.contentColor,
            shape
        ),
        onClick = onClick,
        colors = colors,
        shape = shape,
        content = content
    )
}

@Composable
fun SmallToggleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        containerColor = DesignTheme.colors.background,
        contentColor = DesignTheme.colors.content
    ),
    shape: Shape = ButtonDefaults.shape,
    content: @Composable RowScope.() -> Unit
) {
}
