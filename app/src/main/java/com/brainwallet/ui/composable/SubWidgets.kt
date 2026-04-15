package com.brainwallet.ui.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun screenHeightPercent(percent: Float): Dp {
    return LocalConfiguration.current.screenHeightDp.dp * percent
}
