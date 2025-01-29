package com.brainwallet.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SeedWordItem(
    modifier: Modifier = Modifier,
    number: Int,
    word: String
) {
    Box(
        modifier = modifier
            .background(
                Color(0xFF2C2C2C),
                shape = RoundedCornerShape(64.dp),
            )
            .padding(
                horizontal = 16.dp,
                vertical = 12.dp,
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "$number $word",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}