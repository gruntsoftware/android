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
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.brainwallet.R
import com.brainwallet.data.model.AppSetting
import com.brainwallet.navigation.OnNavigate
import com.brainwallet.navigation.Route
import com.brainwallet.navigation.UiEffect
import com.brainwallet.ui.composable.BrainwalletScaffold
import com.brainwallet.ui.composable.BrainwalletTopAppBar
import com.brainwallet.ui.composable.LargeButton
import com.brainwallet.ui.composable.SeedWordItem
import com.brainwallet.ui.composable.SeedWordsLayout
import com.brainwallet.ui.composable.utils.AutoScrollController
import com.brainwallet.ui.composable.utils.rememberAutoScrollController
import com.brainwallet.ui.theme.BrainwalletAppTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun YourSeedProveItScreen(
    onNavigate: OnNavigate,
    seedWords: List<String>,
    modifier: Modifier = Modifier,
    viewModel: YourSeedProveItViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val clickAudioPlayer = remember { MediaPlayer.create(context, R.raw.clickseedword) }
    val errorAudioPlayer = remember { MediaPlayer.create(context, R.raw.errorsound) }
    val coinAudioPlayer = remember { MediaPlayer.create(context, R.raw.coinflip) }

    LaunchedEffect(Unit) { viewModel.onEvent(YourSeedProveItEvent.OnLoad(seedWords)) }
    LaunchedEffect(state.orderCorrected) { if (state.orderCorrected) coinAudioPlayer.start() }

    YourSeedProveItScreen(
        state = state,
        modifier = modifier,
        onNavigate = onNavigate,
        onEvent = viewModel::onEvent,
        onCorrect = { clickAudioPlayer.start() },
        onWrong = { errorAudioPlayer.start() }
    )
}

@Composable
private fun YourSeedProveItScreen(
    state: YourSeedProveItState,
    modifier: Modifier = Modifier,
    onNavigate: OnNavigate = {},
    onEvent: (YourSeedProveItEvent) -> Unit = {},
    onCorrect: () -> Unit = {},
    onWrong: () -> Unit = {}
) {
    val scrollState = rememberScrollState()
    val autoScrollController = rememberAutoScrollController(scrollState)
    BrainwalletScaffold(
        modifier = modifier,
        topBar = {
            BrainwalletTopAppBar(
                navigationIcon = {
                    IconButton(onClick = { onNavigate.invoke(UiEffect.Navigate.Back()) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(12.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // --- Header ---
            Text(
                text = stringResource(
                    if (state.orderCorrected) R.string.you_saved_your_keys
                    else R.string.you_saved_it_right
                ),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = stringResource(
                    if (state.orderCorrected) R.string.you_saved_your_keys_desc
                    else R.string.you_saved_it_right_desc
                ),
                style = MaterialTheme.typography.bodyMedium.copy(
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            )

            Spacer(modifier = Modifier.weight(0.1f))

            // --- Correct words area ---
            SeedWordsLayout {
                itemsIndexed(state.correctSeedWords.values.toList()) { index, (expected, actual) ->
                    val label =
                        if (expected != actual && actual.isEmpty()) "${index + 1}"
                        else "${index + 1} $actual"

                    SeedWordItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .dragAndDropTarget(
                                shouldStartDragAndDrop = { e ->
                                    e.mimeTypes().contains(ClipDescription.MIMETYPE_TEXT_PLAIN)
                                },
                                target = remember {
                                    seedWordTarget(
                                        index = index,
                                        expectedWord = expected,
                                        onEvent = onEvent,
                                        onCorrect = onCorrect,
                                        onWrong = onWrong,
                                        autoScrollController = autoScrollController
                                    )
                                }
                            ),
                        label = label,
                        isError = actual.isNotEmpty() && expected != actual,
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // --- Instructions ---
            Text(
                text = stringResource(
                    if (state.orderCorrected) R.string.empty_string
                    else R.string.tap_drag_a_word
                ),
                style = MaterialTheme.typography.bodyMedium.copy(
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            )

            Spacer(modifier = Modifier.weight(0.1f))

            // --- Shuffled words area ---
            SeedWordsLayout {
                itemsIndexed(state.shuffledSeedWords) { index, (correctIndex, word) ->
                    if (state.isWordUsedCorrectly(correctIndex, word)) {
                        Box(modifier = Modifier.fillMaxWidth())
                    } else {
                        SeedWordItem(
                            modifier = Modifier
                                .fillMaxWidth()
                                .dragAndDropSource {
                                    detectTapGestures(
                                        onPress = {
                                            startTransfer(
                                                DragAndDropTransferData(
                                                    clipData = ClipData.newPlainText("text", word),
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

            // --- Action button ---
            LargeButton(
                onClick = {
                    if (state.orderCorrected) {
                        onNavigate.invoke(UiEffect.Navigate(Route.TopUp))
                    } else {
                        onEvent(YourSeedProveItEvent.OnClear)
                    }
                }
            ) {
                Text(
                    text = stringResource(
                        if (state.orderCorrected) R.string.game_and_sync
                        else R.string.reset_start_over
                    ).uppercase(),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

private fun seedWordTarget(
    index: Int,
    expectedWord: String,
    onEvent: (YourSeedProveItEvent) -> Unit,
    onCorrect: () -> Unit,
    onWrong: () -> Unit,
    autoScrollController: AutoScrollController,
): DragAndDropTarget = object : DragAndDropTarget {
    override fun onStarted(event: DragAndDropEvent) = autoScrollController.update(event)
    override fun onEntered(event: DragAndDropEvent) = autoScrollController.update(event)
    override fun onMoved(event: DragAndDropEvent) = autoScrollController.update(event)
    override fun onChanged(event: DragAndDropEvent) = autoScrollController.update(event)
    override fun onExited(event: DragAndDropEvent) = autoScrollController.stop()
    override fun onEnded(event: DragAndDropEvent) = autoScrollController.stop()

    override fun onDrop(event: DragAndDropEvent): Boolean {
        autoScrollController.stop()
        val word = event.toAndroidDragEvent().clipData?.getItemAt(0)?.text?.toString().orEmpty()
        onEvent(
            YourSeedProveItEvent.OnDropSeedWordItem(
                index = index,
                expectedWord = expectedWord,
                actualWord = word
            )
        )
        if (word == expectedWord) onCorrect() else onWrong()
        return true
    }
}

@PreviewLightDark
@Composable
private fun YourSeedProveItScreenPreview() {
    BrainwalletAppTheme(appSetting = AppSetting(isSystemInDarkTheme())) {
        val dummyState = YourSeedProveItState(
            correctSeedWords = mapOf(
                0 to SeedWordItem(expected = "apple", actual = "apple"),
                1 to SeedWordItem(expected = "banana", actual = "mango"), // wrong
                2 to SeedWordItem(expected = "cherry", actual = "")
            ),
            shuffledSeedWords = listOf(
                0 to "apple",
                1 to "mango",
                2 to "cherry"
            ),
            orderCorrected = false
        )

        YourSeedProveItScreen(
            state = dummyState
        )
    }
}
