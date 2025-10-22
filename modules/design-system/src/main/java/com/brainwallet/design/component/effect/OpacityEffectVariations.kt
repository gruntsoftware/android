package com.brainwallet.design.component.effect

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Light opacity effect modifier with subtle transparency.
 * Compensated alpha values to maintain original glass effect intensity without blur.
 */
fun Modifier.lightOpacityEffect(
    shape: Shape = RoundedCornerShape(12.dp)
): Modifier = opacityEffect(
    shape = shape,
    backgroundAlpha = 0.08f, // Increased to compensate for missing blur
    borderAlpha = 0.15f, // Increased to compensate for missing blur
    borderWidth = 0.5.dp
)

/**
 * Medium opacity effect modifier with balanced transparency.
 * Compensated alpha values to maintain original glass effect intensity without blur.
 */
fun Modifier.mediumOpacityEffect(
    shape: Shape = RoundedCornerShape(16.dp)
): Modifier = opacityEffect(
    shape = shape,
    backgroundAlpha = 0.12f, // Increased to compensate for missing blur
    borderAlpha = 0.22f, // Increased to compensate for missing blur
    borderWidth = 0.75.dp
)

/**
 * Heavy opacity effect modifier with strong transparency.
 * Compensated alpha values to maintain original glass effect intensity without blur.
 */
fun Modifier.heavyOpacityEffect(
    shape: Shape = RoundedCornerShape(20.dp)
): Modifier = opacityEffect(
    shape = shape,
    backgroundAlpha = 0.18f, // Increased to compensate for missing blur
    borderAlpha = 0.32f, // Increased to compensate for missing blur
    borderWidth = 1.dp
)

/**
 * Card-optimized opacity effect modifier.
 */
fun Modifier.cardOpacityEffect(): Modifier = mediumOpacityEffect(
    shape = RoundedCornerShape(16.dp)
)

/**
 * Navigation-optimized opacity effect modifier.
 */
fun Modifier.navigationOpacityEffect(): Modifier = lightOpacityEffect(
    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
)

/**
 * Drawer-optimized opacity effect modifier.
 * Uses maximum alpha values to compensate for missing blur (was strongest at 24.dp blur).
 */
fun Modifier.drawerOpacityEffect(): Modifier = opacityEffect(
    shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
    backgroundAlpha = 0.25f, // Maximum compensation for missing blur
    borderAlpha = 0.45f, // Maximum compensation for missing blur
    borderWidth = 1.5.dp
)
