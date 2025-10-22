package com.brainwallet.design.component.effect

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Light glass effect modifier with subtle transparency.
 */
fun Modifier.lightGlassEffect(
    shape: Shape = RoundedCornerShape(12.dp)
): Modifier = glassEffect(
    shape = shape,
    backgroundAlpha = 0.06f,
    borderAlpha = 0.12f,
    borderWidth = 0.5.dp
)

/**
 * Medium glass effect modifier with balanced transparency.
 */
fun Modifier.mediumGlassEffect(
    shape: Shape = RoundedCornerShape(16.dp)
): Modifier = glassEffect(
    shape = shape,
    backgroundAlpha = 0.08f,
    borderAlpha = 0.18f,
    borderWidth = 0.75.dp
)

/**
 * Heavy glass effect modifier with strong transparency.
 */
fun Modifier.heavyGlassEffect(
    shape: Shape = RoundedCornerShape(20.dp)
): Modifier = glassEffect(
    shape = shape,
    backgroundAlpha = 0.12f,
    borderAlpha = 0.25f,
    borderWidth = 1.dp
)

/**
 * Card-optimized glass effect modifier.
 */
fun Modifier.cardGlassEffect(): Modifier = mediumGlassEffect(
    shape = RoundedCornerShape(16.dp)
)

/**
 * Navigation-optimized glass effect modifier.
 */
fun Modifier.navigationGlassEffect(): Modifier = lightGlassEffect(
    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
)

/**
 * Drawer-optimized glass effect modifier.
 */
fun Modifier.drawerGlassEffect(): Modifier = heavyGlassEffect(
    shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
)
