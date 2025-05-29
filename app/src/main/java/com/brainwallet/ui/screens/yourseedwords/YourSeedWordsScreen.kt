@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.brainwallet.ui.screens.yourseedwords

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.R
import com.brainwallet.navigation.OnNavigate
import com.brainwallet.navigation.UiEffect
import com.brainwallet.ui.composable.BrainwalletScaffold
import com.brainwallet.ui.composable.BrainwalletTopAppBar
import com.brainwallet.ui.composable.LargeButton
import com.brainwallet.ui.composable.SeedWordItem
import com.brainwallet.ui.composable.SeedWordsLayout
import org.koin.compose.koinInject

@Composable
fun YourSeedWordsScreen(
    onNavigate: OnNavigate,
    seedWords: List<String>,
    viewModel: YourSeedWordsViewModel = koinInject()
) {
    /// Layout values
    val columnPadding = 12
    val horizontalVerticalSpacing = 8
    val spacerHeight = 36
    val leadingCopyPadding = 8
    val detailLineHeight = 24

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is UiEffect.Navigate -> onNavigate.invoke(effect)
                else -> Unit
            }
        }
    }

    BrainwalletScaffold(
        topBar = {
            BrainwalletTopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = { onNavigate.invoke(UiEffect.Navigate.Back()) },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(columnPadding.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(horizontalVerticalSpacing.dp),
        ) {
            Text(
                text = stringResource(R.string.your_seed_words),
                style = MaterialTheme.typography.headlineSmall,
            )

            //todo: yuana private key text need to open dialog?
            Text(
                modifier = Modifier
                    .padding(top = leadingCopyPadding.dp),
                text = stringResource(R.string.your_seed_words_desc),
                style = MaterialTheme.typography.bodyLarge.copy(
                    textAlign = TextAlign.Center,
                    lineHeight = detailLineHeight.sp,
                    color = Color.Gray
                )
            )

            Spacer(modifier = Modifier.weight(0.1f))

            SeedWordsLayout(modifier = Modifier.weight(1f)) {
                itemsIndexed(items = seedWords) { index, word ->
                    SeedWordItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        label = "${index + 1} $word"
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.1f))

            Text(
                text = stringResource(R.string.blockchain_litecoin),
                style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center)
            )

            Spacer(modifier = Modifier.weight(0.2f))

            LargeButton(
                onClick = {
                    viewModel.onEvent(YourSeedWordsEvent.OnSavedItClick(seedWords))
                },
            ) {
                Text(
                    text = stringResource(R.string.i_saved_it_on_paper),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}