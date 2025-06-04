package com.brainwallet.ui.screens.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.brainwallet.R
import com.brainwallet.navigation.OnNavigate
import com.brainwallet.navigation.Route
import com.brainwallet.navigation.UiEffect
import com.brainwallet.tools.util.BRConstants
import com.brainwallet.ui.composable.BorderedLargeButton
import com.brainwallet.ui.composable.BrainwalletButton
import com.brainwallet.ui.composable.DarkModeToggleButton
import com.brainwallet.ui.composable.bottomsheet.FiatSelectorBottomSheet
import com.brainwallet.ui.composable.bottomsheet.LanguageSelectorBottomSheet
import com.brainwallet.ui.theme.BrainwalletTheme
import org.koin.compose.koinInject

@Composable
fun WelcomeScreen(
    onNavigate: OnNavigate = {},
    viewModel: WelcomeViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp
    val density = LocalDensity.current.density
    val mainBoxFactor = if (density > 2) 0.5 else 0.4
    val thirdOfScreenHeight = (screenHeight * mainBoxFactor).toInt()

    LaunchedEffect(Unit) {
        viewModel.onEvent(WelcomeEvent.OnLoad(context))
    }

    //todo: the following sizing can be move to BrainwalletTheme
    // Global layout
    val buttonFontSize = 24
    val thinButtonFontSize = 22
    val toggleButtonSize = 45
    val leadTrailPadding = 8
    val rowPadding = 8
    val versionPadding = 8
    val activeRowHeight = 58

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.welcomeemoji20250212))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrainwalletTheme.colors.surface)
            .verticalScroll(rememberScrollState()),
    ) {
        Image(
            painterResource(R.drawable.brainwallet_logotype_white),
            contentDescription = "brainwallet_logotype_white",
            contentScale = ContentScale.Fit,
            colorFilter = ColorFilter.tint(
                BrainwalletTheme.colors.content,
            ),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 55.dp)
                .padding(vertical = 30.dp)
        )

        // Animation Placeholder
        LottieAnimation(
            modifier = Modifier
                .offset(y = 120.dp)
                .fillMaxWidth()
                .padding(leadTrailPadding.dp)
                .background(
                    BrainwalletTheme.colors.surface,
                    BrainwalletTheme.shapes.large
                )
                .height(thirdOfScreenHeight.dp)
                .clip(BrainwalletTheme.shapes.large),
            composition = composition,
            contentScale = ContentScale.FillWidth,
            alignment = Alignment.Center,
            progress = { progress }
        )

        Column(
            modifier = Modifier.align(Alignment.BottomCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(activeRowHeight.dp)
                    .padding(horizontal = leadTrailPadding.dp)
                    .padding(vertical = rowPadding.dp),
                horizontalArrangement = Arrangement.SpaceEvenly

            ) {

                BrainwalletButton(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    onClick = {
                        viewModel.onEvent(WelcomeEvent.OnLanguageSelectorButtonClick)
                    }
                ) {
                    Text(
                        text = state.selectedLanguage.title,
                        fontSize = 14.sp,
                        color = BrainwalletTheme.colors.content
                    )
                }

                Spacer(modifier = Modifier.weight(0.1f))

                DarkModeToggleButton(
                    modifier = Modifier
                        .width(toggleButtonSize.dp)
                        .aspectRatio(1f),
                    checked = state.darkMode,
                    onCheckedChange = {
                        viewModel.onEvent(WelcomeEvent.OnToggleDarkMode)
                    }
                )

                Spacer(modifier = Modifier.weight(0.1f))

                BrainwalletButton(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    onClick = { viewModel.onEvent(WelcomeEvent.OnFiatButtonClick) }
                ) {
                    Text(
                        text = state.selectedCurrency.name,
                        fontSize = 14.sp,
                        color = BrainwalletTheme.colors.content
                    )
                }

            }
            // Ready Button
            BorderedLargeButton(
                onClick = {
                    onNavigate.invoke(UiEffect.Navigate(Route.Ready))
                },
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .padding(horizontal = leadTrailPadding.dp)
                    .height(activeRowHeight.dp)

            ) {
                Text(
                    text = stringResource(R.string.ready),
                    fontSize = buttonFontSize.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            // Restore Button
            BorderedLargeButton(
                onClick = {
                    onNavigate.invoke(UiEffect.Navigate(Route.InputWords()))
                },
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .testTag("buttonRestore")
                    .padding(horizontal = leadTrailPadding.dp)
                    .height(activeRowHeight.dp)
                    .clip(RoundedCornerShape(50))
            ) {
                Text(
                    text = stringResource(R.string.restore),
                    fontSize = thinButtonFontSize.sp,
                    fontWeight = FontWeight.Thin,
                )
            }

            Text(
                modifier = Modifier.padding(vertical = versionPadding.dp),
                text = BRConstants.APP_VERSION_NAME_CODE,
                fontSize = 13.sp,
                color = BrainwalletTheme.colors.content
            )
        }
    }

    //language selector
    if (state.languageSelectorBottomSheetVisible) {
        LanguageSelectorBottomSheet(
            selectedLanguage = state.selectedLanguage,
            onLanguageSelect = { language ->
                viewModel.onEvent(
                    WelcomeEvent.OnLanguageChange(
                        language
                    )
                )
            },
            onDismissRequest = {
                viewModel.onEvent(WelcomeEvent.OnLanguageSelectorDismiss)
            },
        )
    }

    //fiat/currency selector
    if (state.fiatSelectorBottomSheetVisible) {
        FiatSelectorBottomSheet(
            selectedCurrency = state.selectedCurrency,
            onFiatSelect = {
                viewModel.onEvent(WelcomeEvent.OnFiatChange(it))
            },
            onDismissRequest = {
                viewModel.onEvent(WelcomeEvent.OnFiatSelectorDismiss)
            }
        )
    }
}


@Preview
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen()
}