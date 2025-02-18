package com.brainwallet.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.brainwallet.ui.theme.BrainwalletTheme

@Composable
fun PinDotItem(
    modifier: Modifier = Modifier,
    checked: Boolean = false
) {
    Box(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .size(16.dp)
            .background(if (checked) BrainwalletTheme.colors.content else Color.Transparent, CircleShape)
            .border(1.dp, BrainwalletTheme.colors.content, CircleShape),

        )
}