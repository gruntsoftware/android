package com.brainwallet.ui.composable

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Text
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.ui.theme.BrainwalletAppTheme
import com.brainwallet.ui.theme.BrainwalletTheme

@Composable
fun FiatDropdown(
    selectedFiat: String,
    isDarkTheme: Boolean,
    onFiatSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val languages = listOf("EUR", "USD", "IDR", "¥")
    val buttonFactor = 50
    val buttonWidth = 155

    Box(
        modifier = Modifier.wrapContentSize(Alignment.TopStart)
    ) {
        Button(
            onClick = { expanded = true },
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrainwalletTheme.colors.surface,
                contentColor = BrainwalletTheme.colors.content
            ),
            modifier = Modifier
                .width(buttonWidth.dp)
                .border(1.dp, BrainwalletTheme.colors.border,
                    RoundedCornerShape(buttonFactor.dp))
                .height(50.dp)
        ) {
            Text(text = selectedFiat,
                fontSize = 14.sp,
                color = BrainwalletTheme.colors.content)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
//            languages.forEach { language ->
//                DropdownMenuItem(onClick = {
//                    onFiatSelected(language)
//                    expanded = false
//                }) {
//                    Text(text = language)
//                }
//            }
        }
    }
}