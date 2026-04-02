package com.brainwallet.ui.bentosections.transactionbento

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ConfirmationStatus(
    modifier: Modifier = Modifier,
    numberOfConfs: Int = 0
) {
    Box(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .size(16.dp)
    )
}
