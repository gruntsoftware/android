package com.brainwallet.ui.screens.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.R
import com.brainwallet.navigation.OnNavigate
import com.brainwallet.navigation.Route
import com.brainwallet.navigation.UiEffect
import com.brainwallet.ui.composable.BorderedLargeButton
import com.brainwallet.ui.composable.BrainwalletButton
import com.brainwallet.ui.composable.DarkModeToggleButton
import com.brainwallet.ui.composable.bottomsheet.FiatSelectorBottomSheet
import com.brainwallet.ui.composable.bottomsheet.LanguageSelectorBottomSheet
import com.brainwallet.constants.BWConstants
import com.brainwallet.constants.bentoCornerRadius
import com.brainwallet.ui.bentosections.gamehubbento.FallinScene
import com.brainwallet.ui.theme.BoldenVan
import com.brainwallet.ui.theme.DesignTheme
import com.brainwallet.ui.theme.gameTitleGradient
import kotlinx.coroutines.delay
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

    // todo: the following sizing can be move to BrainwalletTheme
    // Global layout
    val buttonFontSize = 24
    val thinButtonFontSize = 22
    val toggleButtonSize = 45
    val leadTrailPadding = 8
    val rowPadding = 8
    val versionPadding = 8
    val activeRowHeight = 58

    val gameHubBackground = R.drawable.game_hub_bk

    val fullText = "FALLINMOJI"
    var displayedText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        fullText.forEachIndexed { index, _ ->
            delay(120L) // ms per character
            displayedText = fullText.substring(0, index + 1)
        }
        delay(5000L)
        displayedText = ""
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DesignTheme.colors.surface)
            .verticalScroll(rememberScrollState())
            .padding(WindowInsets.systemBars.asPaddingValues()),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(vertical = 20.dp)
        ) {
            Image(
                painterResource(R.drawable.brainwallet_logotype_white),
                contentDescription = "brainwallet_logotype_white",
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(
                    DesignTheme.colors.content,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 55.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(thirdOfScreenHeight.dp)
            ) {
                Image(
                    painter = painterResource(gameHubBackground),
                    contentDescription = "game_hub_background",
                    modifier = Modifier.fillMaxWidth()
                        .padding(leadTrailPadding.dp)
                        .height(thirdOfScreenHeight.dp)
                        .clip(RoundedCornerShape(bentoCornerRadius)),
                    contentScale = ContentScale.Crop,
                )
                FallinScene(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(leadTrailPadding.dp)
                        .height(thirdOfScreenHeight.dp)
                        .clip(DesignTheme.shapes.large),
                    dotQuantity = 24
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth().padding(top = 48.dp)
                        .height(65.dp),
                    Alignment.Center
                ) {
                    Text(
                        text = displayedText,
                        style = TextStyle(
                            fontFamily = BoldenVan,
                            fontWeight = FontWeight.Normal,
                            fontSize = 60.sp,
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                offset = Offset(x = 4f, y = 4f),
                                blurRadius = 4f
                            )
                        ),
                        maxLines = 1,
                    )
                    Text(
                        text = displayedText,
                        style = TextStyle(
                            brush = gameTitleGradient,
                            fontFamily = BoldenVan,
                            fontWeight = FontWeight.Normal,
                            fontSize = 60.sp
                        ),
                        maxLines = 1,
                    )
                }
            }
        }

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
                        color = DesignTheme.colors.content
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
                        color = DesignTheme.colors.content
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
                    text = stringResource(R.string.MenuViewController_createButton),
                    fontSize = buttonFontSize.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            // Restore Button
            BorderedLargeButton(
                onClick = {
                    onNavigate.invoke(UiEffect.Navigate(Route.Restore()))
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
                text = BWConstants.APP_VERSION_NAME_CODE,
                fontSize = 13.sp,
                color = DesignTheme.colors.content
            )
        }
    }

    // language selector
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

    // fiat/currency selector
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
