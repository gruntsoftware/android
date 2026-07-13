@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.brainwallet.ui.screens.emojis
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.R
import com.brainwallet.navigation.OnNavigate
import com.brainwallet.navigation.UiEffect
import com.brainwallet.ui.composable.BrainwalletScaffold
import com.brainwallet.ui.composable.BrainwalletTopAppBar
import com.brainwallet.ui.composable.LargeButton
import com.brainwallet.ui.screens.main.MainScreenEvent
import com.brainwallet.ui.screens.main.MainViewModel
import com.brainwallet.ui.theme.DesignTheme
import com.brainwallet.ui.theme.IBMPlexSans
import kotlinx.collections.immutable.ImmutableList
import org.koin.compose.koinInject

@Composable
fun YourEmojisScreen(
    onNavigate: OnNavigate,
    emojis: ImmutableList<String>,
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = koinInject()
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

            EmojisLayout(modifier = Modifier.weight(1f)) {
                itemsIndexed(items = emojis) { index, emoji ->
                    EmojiItem(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        indexLabel = "${index + 1}",
                        emoji = emoji
                    )
                }
            }

            Spacer(modifier = Modifier.weight(0.1f))

            LargeButton(
                onClick = {
                    viewModel.onEvent(MainScreenEvent.OnUserClearsEmojis)
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

@Composable
fun EmojisLayout(
    modifier: Modifier = Modifier,
    content: LazyGridScope.() -> Unit
) {
    LazyVerticalGrid(
        modifier = modifier
            .height(220.dp),
        columns = GridCells.Fixed(3), // fixed 3
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content
    )
}

@Composable
fun EmojiItem(
    indexLabel: String,
    emoji: String,
    modifier: Modifier = Modifier

) {
    Box(
        modifier = Modifier
            .background(
                color = DesignTheme.colors.background.copy(alpha = 0.3f),
                shape = MaterialTheme.shapes.extraLarge
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center

    ) {
        Text(
            text = indexLabel,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = 4.dp, y = (-2).dp),
            style = TextStyle(
                fontFamily = IBMPlexSans,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp
            ),
            color = DesignTheme.colors.content,
        )

        Text(
            text = emoji,
            style = TextStyle(
                fontFamily = IBMPlexSans,
                fontWeight = FontWeight.Normal,
                fontSize = 26.sp
            ),
        )
    }
}
