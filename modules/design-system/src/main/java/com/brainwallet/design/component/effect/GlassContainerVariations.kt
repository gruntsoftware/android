package com.brainwallet.design.component.effect

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Light glass container with subtle blur and transparency effects.
 */
@Composable
fun LightGlassContainer(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    content: @Composable () -> Unit
) {
    GlassContainer(
        modifier = modifier,
        shape = shape,
        blurRadius = 6.dp,
        backgroundAlpha = 0.06f,
        borderAlpha = 0.12f,
        borderWidth = 0.5.dp,
        content = content
    )
}

/**
 * Medium glass container with balanced blur and transparency effects.
 */
@Composable
fun MediumGlassContainer(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable () -> Unit
) {
    GlassContainer(
        modifier = modifier,
        shape = shape,
        blurRadius = 10.dp,
        backgroundAlpha = 0.08f,
        borderAlpha = 0.18f,
        borderWidth = 0.75.dp,
        content = content
    )
}

/**
 * Heavy glass container with strong blur and transparency effects.
 */
@Composable
fun HeavyGlassContainer(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    content: @Composable () -> Unit
) {
    GlassContainer(
        modifier = modifier,
        shape = shape,
        blurRadius = 16.dp,
        backgroundAlpha = 0.12f,
        borderAlpha = 0.25f,
        borderWidth = 1.dp,
        content = content
    )
}

/**
 * Card-optimized glass container with medium glass effects.
 */
@Composable
fun CardGlassContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    MediumGlassContainer(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        content = content
    )
}

/**
 * Drawer-optimized glass container with extra strong blur for better content visibility.
 */
@Composable
fun DrawerGlassContainer(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
    content: @Composable () -> Unit
) {
    GlassContainer(
        modifier = modifier,
        shape = shape,
        blurRadius = 24.dp,
        backgroundAlpha = 0.18f,
        borderAlpha = 0.35f,
        borderWidth = 1.5.dp,
        content = content
    )
}
