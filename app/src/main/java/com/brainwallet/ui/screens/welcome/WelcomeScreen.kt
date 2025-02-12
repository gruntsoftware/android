@file:OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalComposeUiApi::class
)

package com.brainwallet.ui.screens.welcome

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.rememberLottieAnimatable
import com.airbnb.lottie.compose.rememberLottieComposition
import com.brainwallet.R
import com.brainwallet.ui.composable.Button
import com.brainwallet.ui.composable.FiatDropdown
import com.brainwallet.ui.composable.LanguageDropdown
import com.brainwallet.ui.composable.LargeButton
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalConfiguration
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.animateLottieCompositionAsState

@Composable
fun WelcomeScreen(
    onEvent: (WelcomeEvent) -> Unit = {},
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        // Global layout

        val configuration = LocalConfiguration.current
        val screenHeight = configuration.screenHeightDp
        val screenWidth = configuration.screenWidthDp
        var mainBoxFactor = 0.5
        val thirdOfScreenHeight = (screenHeight * mainBoxFactor).toInt()

        val leadTrailPadding =  16
        val halfLeadTrailPadding =  8
        val rowPadding =  6
        val buttonBorder =  2
        val activeRowHeight =  60
        var selectedLanguage by remember { mutableStateOf("Français") }
        var selectedFiat by remember { mutableStateOf("EUR") }
        val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.welcomeemoji20250212))
        var isPlaying by remember { mutableStateOf(true) }
        val flyingEmojisProgress by animateLottieCompositionAsState(
            composition = composition,
            isPlaying = isPlaying
        )

    LaunchedEffect( key1 = flyingEmojisProgress) {
        if (flyingEmojisProgress ==0f){
            isPlaying = true
        }
        if (flyingEmojisProgress ==1f){
            isPlaying = false
        }
    }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.weight(1f))

            Text(
                modifier = Modifier.background(Color.Red)
                    .padding(leadTrailPadding.dp),
                text = "TEST",
            )

            // Animation Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(thirdOfScreenHeight.dp)
                    .padding(leadTrailPadding.dp),
                contentAlignment = Alignment.Center,
            ) {

                LottieAnimation(
                    composition = composition,
                    isPlaying = isPlaying
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                .height(activeRowHeight.dp)
                .padding(horizontal = halfLeadTrailPadding.dp)
                .padding(vertical = rowPadding.dp)
                .background(
                    color = Color.Red
                ),
                horizontalArrangement = Arrangement.SpaceEvenly

            ) {
                LanguageDropdown(
                    selectedLanguage,
                    isDarkTheme = true
                ) { newLanguage ->
                    selectedLanguage = newLanguage
                }

                //SettingsDayAndNight()
                FiatDropdown(selectedFiat, isDarkTheme = true) { newFiat ->
                    selectedFiat = newFiat
                }
            }
            // Ready Button
            LargeButton(
                onClick = {

                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black), // Black background
                modifier = Modifier
                    .padding(horizontal = halfLeadTrailPadding.dp)
                    .padding(vertical = rowPadding.dp)
                    .border(
                            buttonBorder.dp,
                            Color.Black,
                        RoundedCornerShape(50)
                        )
                        .fillMaxWidth()
                    .height(activeRowHeight.dp)

            ) {
                Text(
                    text = "Ready",
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }

            // Restore Button
            LargeButton(
                onClick = {

                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black), // Black background
                modifier = Modifier
                    .padding(horizontal = halfLeadTrailPadding.dp)
                    .padding(vertical = rowPadding.dp)
                    .border(
                        buttonBorder.dp,
                        Color.Black,
                        RoundedCornerShape(50)
                    ) // White border
                    .fillMaxWidth()
                    .height(activeRowHeight.dp)
            ) {
                Text(
                    text = "Restore",
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.weight(0.2f))

        }
    }
}

@Preview
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen()
}