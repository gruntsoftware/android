package com.brainwallet.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.brainwallet.constants.bentoBorderWidth
import com.brainwallet.constants.bentoCornerRadius
import com.brainwallet.constants.mainBentoBorderWidth

@Suppress("ModifierComposable")
@Composable
fun Modifier.mainBentoSurface(
    isDarkMode: Boolean
): Modifier = this
    .background(
        brush = if (isDarkMode) bentoDarkSurfaceGradient else bentoLightSurfaceGradient,
        shape = RoundedCornerShape(bentoCornerRadius)
    )
    .border(
        width = mainBentoBorderWidth,
        brush = if (isDarkMode) bentoDarkBorderGradient else bentoLightBorderGradient,
        shape = RoundedCornerShape(bentoCornerRadius)
    )

@Suppress("ModifierComposable")
@Composable
fun Modifier.balanceGameBentoSurface(
    isDarkMode: Boolean
): Modifier = this
    .background(
        brush = if (isDarkMode) balanceBackgroundGradient else bentoLightSurfaceGradient,
        shape = RoundedCornerShape(bentoCornerRadius)
    )
    .border(
        width = bentoBorderWidth,
        brush = if (isDarkMode) bentoBalanceGameDarkBorderGradient else bentoLightBorderGradient,
        shape = RoundedCornerShape(bentoCornerRadius)
    )
