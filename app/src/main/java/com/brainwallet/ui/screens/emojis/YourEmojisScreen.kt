@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.brainwallet.ui.screens.emojis
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import kotlinx.collections.immutable.ImmutableList

@Composable
fun YourEmojisScreen(
    onNavigate: OnNavigate,
    emojis: ImmutableList<String>,
    modifier: Modifier = Modifier
) {
    val columnPadding = 12
    val horizontalVerticalSpacing = 8
    val leadingCopyPadding = 8
    val detailLineHeight = 24

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
                text = stringResource(R.string.your_emojis_title),
                style = MaterialTheme.typography.headlineSmall,
            )

            Text(
                modifier = Modifier
                    .padding(top = leadingCopyPadding.dp),
                text = stringResource(R.string.your_emojis_description),
                style = MaterialTheme.typography.bodyLarge.copy(
                    textAlign = TextAlign.Center,
                    lineHeight = detailLineHeight.sp,
                    color = Color.Gray
                )
            )

            Spacer(modifier = Modifier.weight(0.1f))

            SeedWordsLayout(modifier = Modifier.weight(1f)) {
                itemsIndexed(items = emojis) { index, emoji ->
                    SeedWordItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        label = "${index + 1} $emoji"
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.1f))

            LargeButton(
                onClick = {
                    // viewModel.onEvent(YourSeedWordsEvent.OnSavedItClick(seedWords))
                },
            ) {
                Text(
                    text = stringResource(R.string.your_emojis_reset),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
