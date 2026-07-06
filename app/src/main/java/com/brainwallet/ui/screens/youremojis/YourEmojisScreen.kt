@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.brainwallet.ui.screens.youremojis

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.brainwallet.R
import com.brainwallet.navigation.OnNavigate
import com.brainwallet.navigation.UiEffect
import com.brainwallet.ui.composable.BrainwalletScaffold
import com.brainwallet.ui.composable.BrainwalletTopAppBar
import com.brainwallet.ui.composable.LargeButton
import kotlinx.collections.immutable.ImmutableList
import org.koin.compose.koinInject

@Composable
fun YourEmojisScreen(
    onNavigate: OnNavigate,
    emojis: ImmutableList<String>,
    modifier: Modifier = Modifier,
    viewModel: YourEmojisViewModel = koinInject()
) {
    // / Layout values
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
                }
            )
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

            Spacer(modifier = Modifier.weight(0.2f))

            LargeButton(
                onClick = {
                    viewModel.onEvent(YourEmojisEvent.OnSavedItClick(emojis))
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
