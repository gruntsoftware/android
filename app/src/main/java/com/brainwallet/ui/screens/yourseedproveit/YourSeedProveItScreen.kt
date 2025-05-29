@file:OptIn(
    ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class,
    ExperimentalFoundationApi::class
)

package com.brainwallet.ui.screens.yourseedproveit

import android.content.ClipData
import android.content.ClipDescription
import android.media.MediaPlayer
import android.view.View
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.brainwallet.R
import com.brainwallet.navigation.OnNavigate
import com.brainwallet.navigation.Route
import com.brainwallet.navigation.UiEffect
import com.brainwallet.ui.composable.BrainwalletScaffold
import com.brainwallet.ui.composable.BrainwalletTopAppBar
import com.brainwallet.ui.composable.LargeButton
import com.brainwallet.ui.composable.SeedWordItem
import com.brainwallet.ui.composable.SeedWordsLayout
import org.koin.compose.koinInject

@Composable
fun YourSeedProveItScreen(
    onNavigate: OnNavigate,
    seedWords: List<String>,
    viewModel: YourSeedProveItViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    /// Layout values
    val columnPadding = 12
    val horizontalVerticalSpacing = 8
    val spacerHeight = 48
    val maxItemsPerRow = 3

    val clickAudioPlayer = remember { MediaPlayer.create(context, R.raw.clickseedword) }
    val coinAudioPlayer = remember { MediaPlayer.create(context, R.raw.coinflip) }

    LaunchedEffect(Unit) {
        viewModel.onEvent(YourSeedProveItEvent.OnLoad(seedWords))
    }

    LaunchedEffect(state.orderCorrected) {
        if (state.orderCorrected) {
            coinAudioPlayer.start()
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
        },
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

            Spacer(modifier = Modifier.weight(0.1f))

            SeedWordsLayout {
                itemsIndexed(items = state.correctSeedWords.values.toList()) { index: Int, (expectedWord, actualWord): SeedWordItem ->
                    val label = if (expectedWord != actualWord && actualWord.isEmpty()) {
                        "${index + 1}"
                    } else {
                        "${index + 1} $actualWord"
                    }

                    SeedWordItem(
                        modifier = Modifier
                            .fillMaxWidth()
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
                                                    index = index,
                                                    expectedWord = expectedWord,
                                                    actualWord = text.toString()
                                                )
                                            )

                                            if (expectedWord == actualWord) {
                                                clickAudioPlayer.start()
                                            }
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

            SeedWordsLayout {
                itemsIndexed(items = state.shuffledSeedWords) { index, word ->

                    val isWordUsedCorrectly =
                        state.correctSeedWords.values.any { (expectedWord, actualWord) ->
                            expectedWord == word && actualWord == word
                        }

                    if (isWordUsedCorrectly) {
                        Box(modifier = Modifier.fillMaxWidth())
                    } else {
                        SeedWordItem(
                            modifier = Modifier
                                .fillMaxWidth()
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


            LargeButton(
                onClick = {
                    if (state.orderCorrected) {
                        onNavigate.invoke(UiEffect.Navigate(Route.TopUp))
                    } else {
                        viewModel.onEvent(YourSeedProveItEvent.OnClear)
                    }
                },
            ) {
                Text(
                    text = stringResource(if (state.orderCorrected) R.string.game_and_sync else R.string.reset_start_over).uppercase(),
                    style = MaterialTheme.typography.labelLarge
                )
            }

        }
    }
}