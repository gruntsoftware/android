package com.brainwallet.design.component.effect

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

/**
 * Light opacity container with subtle transparency effects.
 * Compensated alpha values to maintain original glass effect intensity without blur.
 */
@Composable
fun LightOpacityContainer(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(12.dp),
    content: @Composable () -> Unit
) {
    OpacityContainer(
        modifier = modifier,
        shape = shape,
        backgroundAlpha = 0.08f, // Increased from 0.06f to compensate for missing blur
        borderAlpha = 0.15f, // Increased from 0.12f to compensate for missing blur
        borderWidth = 0.5.dp,
        content = content
    )
}

/**
 * Medium opacity container with balanced transparency effects.
 * Compensated alpha values to maintain original glass effect intensity without blur.
 */
@Composable
fun MediumOpacityContainer(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    content: @Composable () -> Unit
) {
    OpacityContainer(
        modifier = modifier,
        shape = shape,
        backgroundAlpha = 0.12f, // Increased from 0.08f to compensate for missing blur
        borderAlpha = 0.22f, // Increased from 0.18f to compensate for missing blur
        borderWidth = 0.75.dp,
        content = content
    )
}

/**
 * Heavy opacity container with strong transparency effects.
 * Compensated alpha values to maintain original glass effect intensity without blur.
 */
@Composable
fun HeavyOpacityContainer(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    content: @Composable () -> Unit
) {
    OpacityContainer(
        modifier = modifier,
        shape = shape,
        backgroundAlpha = 0.18f, // Increased from 0.12f to compensate for missing blur
        borderAlpha = 0.32f, // Increased from 0.25f to compensate for missing blur
        borderWidth = 1.dp,
        content = content
    )
}

/**
 * Card-optimized opacity container with medium effects.
 */
@Composable
fun CardOpacityContainer(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    MediumOpacityContainer(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        content = content
    )
}

/**
 * Drawer-optimized opacity container with extra strong effects for better content visibility.
 * Compensated alpha values to maintain original glass effect intensity without blur.
 */
@Composable
fun DrawerOpacityContainer(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
    content: @Composable () -> Unit
) {
    OpacityContainer(
        modifier = modifier,
        shape = shape,
        backgroundAlpha = 0.25f, // Increased from 0.18f to compensate for missing blur (was strongest blur at 24.dp)
        borderAlpha = 0.45f, // Increased from 0.35f to compensate for missing blur
        borderWidth = 1.5.dp,
        content = content
    )
}

@Preview()
@Composable
private fun OpacityContainerPreview() {
    BrainwalletTheme(true) {
        OpacityContainer(
            shape = RoundedCornerShape(16.dp),
            backgroundAlpha = 0.18f,
            borderAlpha = 0.35f,
            borderWidth = 1.5.dp
        ) {
            Text("Opacity Container", modifier = Modifier.padding(32.dp), color = Color.White)
        }
    }
}
