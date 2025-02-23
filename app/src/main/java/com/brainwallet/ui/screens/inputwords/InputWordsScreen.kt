@file:OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalComposeUiApi::class
)

package com.brainwallet.ui.screens.inputwords

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brainwallet.R
import com.brainwallet.data.model.AppSetting
import com.brainwallet.navigation.OnNavigate
import com.brainwallet.navigation.Route
import com.brainwallet.navigation.UiEffect
import com.brainwallet.ui.composable.BorderedLargeButton
import com.brainwallet.ui.composable.BrainwalletScaffold
import com.brainwallet.ui.composable.BrainwalletTopAppBar
import com.brainwallet.ui.composable.SeedWordItemTextField
import com.brainwallet.ui.theme.BrainwalletAppTheme
import com.brainwallet.ui.theme.BrainwalletTheme

@Composable
fun InputWordsScreen(
    onNavigate: OnNavigate,
    source: Route.InputWords.Source? = null,
    viewModel: InputWordsViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val focusRequesters = List(12) { FocusRequester() } //12 seed words
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val appSetting = AppSetting()

    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp
    var mainBoxFactor = 0.5
    val thirdOfScreenHeight = (screenHeight * mainBoxFactor).toInt()

    //todo: the following sizing can be move to BrainwalletTheme

    val leadTrailPadding = 24
    val halfLeadTrailPadding = leadTrailPadding / 2
    val doubleLeadTrailPadding = leadTrailPadding * 2
    val buttonMediumFontSize = 20
    val rowPadding = 8
    val activeRowHeight = 60


    LaunchedEffect(Unit) {
        viewModel.onEvent(InputWordsEvent.OnLoad(source))
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is UiEffect.Navigate -> onNavigate.invoke(effect)
                else -> Unit
            }
        }
    }

    /// Layout values
    val columnPadding = 16
    val horizontalVerticalSpacing = 8
    val spacerHeight = 48
    val mediumHeight = 24
    val maxItemsPerRow = 3
    val buttonFontSize = 24

    BrainwalletScaffold(
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        topBar = {
            BrainwalletTopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = { onNavigate.invoke(UiEffect.Navigate.Back()) },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            tint = BrainwalletTheme.colors.content,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(columnPadding.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(horizontalVerticalSpacing.dp),
        ) {
            Spacer(modifier = Modifier.height(mediumHeight.dp))

            Text(
                text = stringResource(R.string.restore_your_power),
                style = MaterialTheme.typography.headlineLarge,
            )

            Spacer(modifier = Modifier.height(columnPadding.dp))

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = horizontalVerticalSpacing.dp),
                text = stringResource(R.string.restore_your_power_desc),
                style = MaterialTheme.typography.titleMedium.copy(
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )

            )

            Spacer(modifier = Modifier.height(mediumHeight.dp))

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(horizontalVerticalSpacing.dp),
                verticalArrangement = Arrangement.spacedBy(horizontalVerticalSpacing.dp),
                maxItemsInEachRow = maxItemsPerRow
            ) {
                state.seedWords.entries.forEach { (index, text) ->
                    SeedWordItemTextField(
                        modifier = Modifier
                            .focusRequester(focusRequesters[index])
                            .weight(1f)
                            .testTag("textFieldSeedWord$index"),
                        value = text,
                        suggestions = state.suggestionsSeedWords,
                        onValueChange = {
                            viewModel.onEvent(
                                InputWordsEvent.OnSeedWordItemChange(
                                    index = index, text = it,
                                )
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            imeAction = if (index < 11) ImeAction.Next else ImeAction.Done
                        ),
                        prefix = {
                            Text("${index + 1}")
                        }
                    )

                }

            }

            Spacer(modifier = Modifier.height(8.dp))

            FilledTonalButton(
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = BrainwalletTheme.colors.surface,
                    contentColor = BrainwalletTheme.colors.content
                ),
                onClick = {
                    viewModel.onEvent(InputWordsEvent.OnClearSeedWords)
                    focusRequesters.first().requestFocus()
                },
            ) {
                Text(stringResource(R.string.clear))
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = horizontalVerticalSpacing.dp),
                text = stringResource(R.string.dont_guess_desc),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color.Gray,
                    lineHeight = 1.6.em
                )
            )
            Spacer(modifier = Modifier.weight(0.5f))


            Text(
                text = stringResource(R.string.blockchain_litecoin),
                style = MaterialTheme.typography.titleMedium.copy(textAlign = TextAlign.Center)
            )

            Spacer(modifier = Modifier.weight(0.5f))

            BorderedLargeButton(
                modifier = Modifier.testTag("buttonRestore")
                    .padding(horizontal = halfLeadTrailPadding.dp)
                    .padding(vertical = rowPadding.dp)
                    .height(activeRowHeight.dp)
                ,
                onClick = {
                    viewModel.onEvent(InputWordsEvent.OnRestoreClick(context = context))
                    focusManager.clearFocus()
                },
            ) {

                Text(
                    text = stringResource(R.string.restore_my_brainwallet),
                    fontSize = buttonMediumFontSize.sp,
                    fontWeight = FontWeight.Thin,
                )
            }

            Spacer(modifier = Modifier.height(horizontalVerticalSpacing.dp))

        }
    }
}
