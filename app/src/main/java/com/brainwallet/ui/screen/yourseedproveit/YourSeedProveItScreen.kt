@file:OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalFoundationApi::class
)

package com.brainwallet.ui.screen.yourseedproveit

import android.content.ClipData
import android.content.ClipDescription
import android.view.View
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.mimeTypes
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brainwallet.R
import com.brainwallet.ui.composable.LargeButton
import com.brainwallet.ui.composable.SeedWordItem

@Composable
fun YourSeedProveItScreen(
    seedWords: List<String>,
    onEvent: (YourSeedProveItEvent) -> Unit = {},
    viewModel: YourSeedProveItViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onEvent(YourSeedProveItEvent.OnLoad(seedWords))
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {}, navigationIcon = {
                IconButton(
                    onClick = { onEvent.invoke(YourSeedProveItEvent.OnBackClick) },
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
                text = stringResource(if (state.orderCorrected) R.string.you_saved_your_keys else R.string.you_saved_it_right),
                style = MaterialTheme.typography.headlineSmall,
            )

            Text(
                text = stringResource(if (state.orderCorrected) R.string.you_saved_your_keys_desc else R.string.you_saved_it_right_desc),
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
                state.correctSeedWords.entries.forEachIndexed { index, (expectedWord, actualWord) ->

                    val label = if (expectedWord != actualWord && actualWord.isEmpty()) {
                        "${index + 1}"
                    } else {
                        "${index + 1} $actualWord"
                    }

                    SeedWordItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .dragAndDropTarget(
                                shouldStartDragAndDrop = { event ->
                                    event
                                        .mimeTypes()
                                        .contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                                },
                                target = remember {
                                    object : DragAndDropTarget {
                                        override fun onDrop(event: DragAndDropEvent): Boolean {
                                            val text = event.toAndroidDragEvent().clipData
                                                ?.getItemAt(0)?.text

                                            viewModel.onEvent(
                                                YourSeedProveItEvent.OnDropSeedWordItem(
                                                    expectedWord = expectedWord,
                                                    actualWord = text.toString()
                                                )
                                            )

                                            return true
                                        }
                                    }
                                }
                            ),
                        label = label,
                        isError = actualWord.isNotEmpty() && expectedWord != actualWord,
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            AnimatedVisibility(visible = state.orderCorrected.not()) {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 3
                ) {
                    state.shuffledSeedWords.forEachIndexed { index, word ->
                        SeedWordItem(
                            modifier = Modifier
                                .dragAndDropSource {
                                    detectTapGestures(
                                        onLongPress = {
                                            startTransfer(
                                                DragAndDropTransferData(
                                                    clipData = ClipData.newPlainText(
                                                        "text",
                                                        word
                                                    ),
                                                    flags = View.DRAG_FLAG_GLOBAL
                                                )
                                            )
                                        }
                                    )
                                },
                            label = word,
                            trailingIcon = {
                                Icon(
                                    painterResource(R.drawable.ui_drag_indicator),
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }
            }

            AnimatedVisibility(visible = state.orderCorrected) {
                LargeButton(
                    onClick = {
                        onEvent.invoke(YourSeedProveItEvent.OnGameAndSync)
                    },
                ) {
                    Text(
                        text = stringResource(R.string.game_and_sync),
                        style = MaterialTheme.typography.labelLarge.copy(color = Color.White) //for now just hardcoded, need to create button composable later and adjust the theme later at [com.brainwallet.ui.theme.Theme]
                    )
                }
            }

        }
    }
}