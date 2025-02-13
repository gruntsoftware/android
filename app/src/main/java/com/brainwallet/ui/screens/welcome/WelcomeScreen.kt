@file:OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalComposeUiApi::class
)

package com.brainwallet.ui.screens.welcome

import android.content.res.Resources.Theme
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
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
import com.airbnb.lottie.compose.rememberLottieComposition
import com.brainwallet.R
import com.brainwallet.ui.composable.FiatDropdown
import com.brainwallet.ui.composable.LanguageDropdown
import com.brainwallet.ui.composable.LargeButton
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.brainwallet.ui.theme.openSauceOneFamily

@Composable
fun WelcomeScreen(
    onEvent: (WelcomeEvent) -> Unit = {},
) {
    // Global layout

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp
    val screenWidth = configuration.screenWidthDp
    var mainBoxFactor = 0.5
    val thirdOfScreenHeight = (screenHeight * mainBoxFactor).toInt()

    val buttonFontSize = 16
    val thinButtonFontSize = 14

    val leadTrailPadding =  24
    val halfLeadTrailPadding =  leadTrailPadding / 2
    val doubleLeadTrailPadding =  leadTrailPadding * 2
    val rowPadding =  8
    val buttonBorder =  1
    val activeRowHeight =  70

    val midnightColor = Color(0xFF0F0853)

    var selectedLanguage by remember { mutableStateOf("Français") }
    var selectedFiat by remember { mutableStateOf("EUR") }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.welcomeemoji20250212))
    val progress by animateLottieCompositionAsState(composition = composition, iterations = LottieConstants.IterateForever)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(midnightColor),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

            Spacer(modifier = Modifier.weight(1f))

            Image(
                painterResource(R.drawable.bw_white_logotype),
                contentDescription = "bw_white_logotype",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(doubleLeadTrailPadding.dp)
            )

            // Animation Placeholder
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(thirdOfScreenHeight.dp)
                    .padding(leadTrailPadding.dp)
                ) {

                LottieAnimation(
                    modifier = Modifier
                        .background(midnightColor),
                    composition = composition,
                    progress = { progress }
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF402DAE)), // Black background
                modifier = Modifier
                    .padding(horizontal = halfLeadTrailPadding.dp)
                    .padding(vertical = rowPadding.dp)
                    .border(
                            buttonBorder.dp,
                            Color.White,
                        RoundedCornerShape(50)
                        )
                        .fillMaxWidth()
                    .height(activeRowHeight.dp)

            ) {
                Text(
                    text = "Ready",
                    fontSize = buttonFontSize.sp,
                    fontFamily = openSauceOneFamily,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Restore Button
            LargeButton(
                onClick = {

                },
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF402DAE)), // Black background
                modifier = Modifier
                    .padding(horizontal = halfLeadTrailPadding.dp)
                    .padding(vertical = rowPadding.dp)
                    .border(
                        buttonBorder.dp,
                        Color.White,
                        RoundedCornerShape(50)
                    ) // White border
                    .fillMaxWidth()
                    .height(activeRowHeight.dp)
            ) {
                Text(
                    text = "Restore",
                    fontSize = thinButtonFontSize.sp,
                    fontFamily = openSauceOneFamily,
                    fontWeight = FontWeight.Thin,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.weight(0.5f))

        }
    }

@Preview
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen()
}