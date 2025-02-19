package com.brainwallet.ui.theme

import androidx.compose.ui.graphics.Color

val midnight = Color(0xFF0F0853)
val grape = Color(0xFF402DAE)
val cheddar = Color(0xFFFFAE00)
val chilli = Color(0xFFCE3025)
val pesto = Color(0xFF25AA2C)
val blue = Color(0xFF2968F2)
val nearBlack = Color(0xFF151515)
val gray = Color(0xFFB8B8B8)
val white = Color.White

//to make color darken, since we are not providing all colors for specific case
inline fun Color.darken(darkenBy: Float = 0.3f): Color {
    return copy(
        red = red * darkenBy,
        green = green * darkenBy,
        blue = blue * darkenBy,
        alpha = alpha
    )
}