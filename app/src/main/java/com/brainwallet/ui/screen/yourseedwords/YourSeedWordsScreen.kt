@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.brainwallet.ui.screen.yourseedwords

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.brainwallet.R
import com.brainwallet.ui.composable.LargeButton
import com.brainwallet.ui.composable.SeedWordItem

@Composable
fun YourSeedWordsScreen(
    seedWords: List<String>,
    onEvent: (YourSeedWordsEvent) -> Unit = {},
) {
    Scaffold(
        topBar = {
            TopAppBar(title = {}, navigationIcon = {
                IconButton(
                    onClick = { onEvent.invoke(YourSeedWordsEvent.OnBackClick) },
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
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.your_seed_words),
                style = MaterialTheme.typography.headlineSmall,
            )

            //todo: yuana private key text need to open dialog?
            Text(
                text = stringResource(R.string.your_seed_words_desc),
                style = MaterialTheme.typography.bodyMedium.copy(
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                maxItemsInEachRow = 3
            ) {
                seedWords.forEachIndexed { index, word ->
                    SeedWordItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        label = "${index + 1} $word"
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = stringResource(R.string.blockchain_litecoin),
                style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center)
            )

            Spacer(modifier = Modifier.weight(1f))

            LargeButton(
                onClick = {
                    onEvent.invoke(YourSeedWordsEvent.OnSavedItClick)
                },
            ) {
                Text(
                    text = stringResource(R.string.i_saved_it_on_paper),
                    style = MaterialTheme.typography.labelLarge.copy(color = Color.White) //for now just hardcoded, need to create button composable later and adjust the theme later at [com.brainwallet.ui.theme.Theme]
                )
            }
        }
    }
}