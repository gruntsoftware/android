package com.brainwallet.ui.bentosections.shopbento

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun GiftCardsComposable(
    cardData: String,
    modifier: Modifier = Modifier,
) {
    val pointerSize = 40.dp
    val mainPadding = 22.dp
    val cornerRadius = 10.dp
    Box(
        modifier = Modifier

    ) {
        RoundedCornerShape(4.dp)
    }
}
