package com.brainwallet.ui.composable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.DropdownMenuItem

@Composable
fun LanguageDropdown(
    selectedLanguage: String,
    isDarkTheme: Boolean,
    onLanguageSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val languages = listOf("Français", "English", "Español", "Deutsch")

    Box(
        modifier = Modifier.wrapContentSize(Alignment.TopStart)
    ) {
        Button(
            onClick = { expanded = true },
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = if (isDarkTheme) Color.Black else Color.White), // Black background
            modifier = Modifier.border(2.dp, if (!isDarkTheme) Color.Black else Color.White, RoundedCornerShape(25.dp)) // White border
                .padding(2.dp)
        ) {
            Text(text = selectedLanguage, fontSize = 12.sp, color = if (!isDarkTheme) Color.Black else Color.White)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            languages.forEach { language ->
//                DropdownMenuItem(
//                    onClick = {
//                        onLanguageSelected(language)
//                    }) {
//                    Text("land")
//                }

//                DropdownMenuItem(
//                    onClick = {
//                        onLanguageSelected(language)
//                    }
//                ) {
//                    Text(text = language)
//                }
            }
        }
    }
}


//DropdownMenuItem(
//onClick = {
//    onLanguageSelected(language)
//    expanded = false
//}) {
//    Text(text = language)
//}
