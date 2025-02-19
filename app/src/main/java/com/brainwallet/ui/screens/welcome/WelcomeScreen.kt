package com.brainwallet.ui.screens.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.brainwallet.R
import com.brainwallet.navigation.OnNavigate
import com.brainwallet.navigation.Route
import com.brainwallet.navigation.UiEffect
import com.brainwallet.ui.composable.BorderedLargeButton
import com.brainwallet.ui.composable.FiatDropdown
import com.brainwallet.ui.composable.LanguageDropdown
import com.brainwallet.ui.theme.BrainwalletTheme

@Composable
fun WelcomeScreen(
    onNavigate: OnNavigate = {},
    viewModel: WelcomeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    // Global layout

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp
    val screenWidth = configuration.screenWidthDp
    var mainBoxFactor = 0.5
    val thirdOfScreenHeight = (screenHeight * mainBoxFactor).toInt()

    //todo: the following sizing can be move to BrainwalletTheme
    val buttonFontSize = 16
    val thinButtonFontSize = 14

    val leadTrailPadding = 24
    val halfLeadTrailPadding = leadTrailPadding / 2
    val doubleLeadTrailPadding = leadTrailPadding * 2
    val rowPadding = 8
    val buttonBorder = 1
    val activeRowHeight = 70

    val midnightColor = Color(0xFF0F0853)

    var selectedLanguage by remember { mutableStateOf("Français") }
    var selectedFiat by remember { mutableStateOf("EUR") }
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.welcomeemoji20250212))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrainwalletTheme.colors.surface),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Spacer(modifier = Modifier.weight(1f))

        Image(
            painterResource(R.drawable.bw_white_logotype),
            contentDescription = "bw_white_logotype",
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(
                BrainwalletTheme.colors.content,
            ),
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
                modifier = Modifier.background(BrainwalletTheme.colors.surface),
                composition = composition,
                progress = { progress }
            )
        }
        Spacer(modifier = Modifier.weight(1f))

//        TODO: implement later, for now just comment this
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(activeRowHeight.dp)
                .padding(horizontal = halfLeadTrailPadding.dp)
                .padding(vertical = rowPadding.dp),
            horizontalArrangement = Arrangement.SpaceEvenly

        ) {
            LanguageDropdown(
                selectedLanguage,
                isDarkTheme = true
            ) { newLanguage ->
                selectedLanguage = newLanguage
            }

            DarkModeToggleButton(
                checked = state.darkMode,
                onCheckedChange = {
                    viewModel.onEvent(WelcomeEvent.OnToggleDarkMode)
                }
            ) {
                Icon(
                    painter = painterResource(if (state.darkMode) R.drawable.ic_light_mode else R.drawable.ic_dark_mode),
                    contentDescription = "toggle-dark-mode"
                )
            }

            FiatDropdown(selectedFiat, isDarkTheme = true) { newFiat ->
                selectedFiat = newFiat
            }

        }
        // Ready Button
        BorderedLargeButton(
            onClick = {
                onNavigate.invoke(UiEffect.Navigate(Route.Ready))
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .padding(horizontal = halfLeadTrailPadding.dp)
                .padding(vertical = rowPadding.dp)
                .height(activeRowHeight.dp)

        ) {
            Text(
                text = stringResource(R.string.ready),
                fontSize = buttonFontSize.sp,
            )
        }

        // Restore Button
        BorderedLargeButton(
            onClick = {
                onNavigate.invoke(UiEffect.Navigate(Route.InputWords()))
            },
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .padding(horizontal = halfLeadTrailPadding.dp)
                .padding(vertical = rowPadding.dp)
                .height(activeRowHeight.dp)
        ) {
            Text(
                text = stringResource(R.string.restore),
                fontSize = thinButtonFontSize.sp,
                fontWeight = FontWeight.Thin,
            )
        }

        Spacer(modifier = Modifier.weight(0.5f))

    }
}

@Composable
fun DarkModeToggleButton(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    IconToggleButton(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        content = content
    )
}

@Preview
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen()
}