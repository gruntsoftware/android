package com.brainwallet.presenter.activities.intro

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.brainwallet.R
import com.brainwallet.presenter.activities.util.ThemePreferences
import com.brainwallet.ui.composable.SettingsDayAndNight
import com.brainwallet.ui.screen.intro.ThemeViewModel
import com.brainwallet.ui.screen.intro.ThemeViewModelFactory
import com.brainwallet.ui.theme.BrainwalletAppTheme


class IntroActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val themePreferences = ThemePreferences(this)
        val viewModelFactory = ThemeViewModelFactory(themePreferences)
        val viewModel = ViewModelProvider(this, viewModelFactory).get(ThemeViewModel::class.java)

        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsState()

            // note for the next developing:
            // pass the view model to navigation for the next activity so it will be persistence across the app
            BrainwalletApp(isDarkMode)
        }
    }
}

@Composable
fun BrainwalletApp(darkMode: Boolean){

    BrainwalletAppTheme(darkTheme = darkMode, dynamicColor = true) {
        Surface(
            modifier = Modifier.fillMaxSize(),
        ) {
            val audio = MediaPlayer()
            BrainwalletUI(darkMode)
        }
    }
}
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
            modifier = Modifier
                .border(2.dp, if (!isDarkTheme) Color.Black else Color.White, RoundedCornerShape(25.dp)) // White border
                .padding(2.dp)
        ) {
            Text(text = selectedLanguage, fontSize = 12.sp, color = if (!isDarkTheme) Color.Black else Color.White)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
//            languages.forEach { language ->
//                DropdownMenuItem(onClick = {
//                    onLanguageSelected(language)
//                    expanded = false
//                }) {
//                    Text(text = language)
//                }
//            }
        }
    }
}

@Composable
fun FiatDropdown(
    selectedFiat: String,
    isDarkTheme: Boolean,
    onFiatSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val languages = listOf("EUR", "USD", "IDR", "¥")

    Box(
        modifier = Modifier.wrapContentSize(Alignment.TopStart)
    ) {
        Button(
            onClick = { expanded = true },
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(containerColor = if (isDarkTheme) Color.Black else Color.White),
            modifier = Modifier
                .border(2.dp, if (!isDarkTheme) Color.Black else Color.White, RoundedCornerShape(25.dp))
                .height(50.dp)
                .width(100.dp)
        ) {
            Text(text = selectedFiat, fontSize = 14.sp, color = if (!isDarkTheme) Color.Black else Color.White)
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


// toggle button
fun DrawScope.drawHalfCircle(rotation: Float) {
    rotate(rotation) {
        // black half circle
        drawArc(
            color = Color.Black,
            startAngle = -90f,
            sweepAngle = 180f,
            useCenter = true,
            size = Size(size.width, size.height),
            topLeft = Offset.Zero
        )
        // white half circle
        drawArc(
            color = Color.White,
            startAngle = 90f,
            sweepAngle = 180f,
            useCenter = true,
            size = Size(size.width, size.height),
            topLeft = Offset.Zero
        )
    }
}

@Composable
fun BrainwalletUI(isDarkTheme: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(if (isDarkTheme) Color.Black else Color.White),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = if (isDarkTheme) {R.drawable.brainwallet_logo_white}
                                            else {R.drawable.brainwallet_logo_black}),
                contentDescription = "Brainwallet Logo",
                modifier = Modifier
                    .height(220.dp)
                    .width(300.dp)
                    .padding(top = 60.dp)
            )
        }

        // Animation Placeholder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .padding(16.dp)
                .background(if (!isDarkTheme) Color.Black else Color.White),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Animation: Language + Fiat + Emoji",
                color = if (isDarkTheme) Color.Black else Color.White,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }

        var selectedLanguage by remember { mutableStateOf("Français") }
        var selectedFiat by remember { mutableStateOf("EUR") }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = if (isDarkTheme) Color.Black else Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    LanguageDropdown(selectedLanguage, isDarkTheme = isDarkTheme) { newLanguage ->
                        selectedLanguage = newLanguage
                    }


                    // Animated rotation to smoothly swap colors
                    val rotation by animateFloatAsState(if (isDarkTheme) 0f else 180f, label = "")

                    // Canvas for custom drawing
//                    Canvas(
//                        modifier = Modifier
//                            .size(48.dp)
//                            .border(1.dp, color = if(isDarkTheme){ Color.White } else {Color.Black}, RoundedCornerShape(30.dp))
//                            .clickable(interactionSource = remember { MutableInteractionSource() }, // No interaction ripple
//                                indication = null) { isDarkTheme = !isDarkTheme} // Toggle state on click
//                    ) {
//                        drawHalfCircle(rotation)
//                    }

//                Switch(
//                    checked = isDarkTheme,
//                    onCheckedChange = { isDarkTheme = it },
//                    modifier = Modifier.padding(horizontal = 8.dp),
//                    colors = SwitchDefaults.colors(
//                        checkedThumbColor = Color.White,
//                        uncheckedThumbColor = Color.Black
//                    )
//                )
                    SettingsDayAndNight()
                    FiatDropdown(selectedFiat, isDarkTheme = isDarkTheme) {newFiat ->
                        selectedFiat = newFiat
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Ready Button
                Button(
                    onClick = { /* Handle Ready Action */ },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isDarkTheme) Color.Black else Color.White), // Black background
                    modifier = Modifier
                        .border(2.dp, if (!isDarkTheme) Color.Black else Color.White, RoundedCornerShape(25.dp)) // White border
                        .fillMaxWidth(0.8f)
                        .height(50.dp)
                ) {
                    Text(text = "Ready!", fontSize = 16.sp, color=if (!isDarkTheme) Color.Black else Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Restore Button
                Button(
                    onClick = { /* Handle Restore Action */ },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = if (isDarkTheme) Color.Black else Color.White), // Black background
                    modifier = Modifier
                        .border(2.dp, if (!isDarkTheme) Color.Black else Color.White, RoundedCornerShape(25.dp)) // White border
                        .fillMaxWidth(0.8f)
                        .height(50.dp)
                ) {
                    Text(text = "Restore", fontSize = 16.sp, color = if (!isDarkTheme) Color.Black else Color.White)
                }
            }
        }


    }


}

@Preview(showBackground = true)
@Composable
fun BrainwalletPreview() {
    BrainwalletApp(isSystemInDarkTheme())
}