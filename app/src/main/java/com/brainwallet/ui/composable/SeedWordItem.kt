package com.brainwallet.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.brainwallet.ui.screen.yourseedproveit.YourSeedProveItScreen

@Composable
fun SeedWordItem(
    modifier: Modifier = Modifier,
    label: String,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
) {
    Box(modifier = modifier) {
        Row(
            modifier = modifier
                .background(
                    color = Color(0xFF2C2C2C),
                    shape = MaterialTheme.shapes.extraLarge
                )
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.padding(vertical = 12.dp),
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isError) Color.Red else Color.White
            )
            trailingIcon?.let { icon ->
                Spacer(modifier = Modifier.width(8.dp))
                icon()
            }
        }
    }
}
