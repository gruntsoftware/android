package com.brainwallet.ui.screens.emojis

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.emoji2.emojipicker.EmojiPickerView
import com.brainwallet.R
import com.brainwallet.constants.BWConstants
import com.brainwallet.tools.manager.AnalyticsManager
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.security.BRKeyStore
import com.brainwallet.ui.theme.DesignTheme
import com.brainwallet.ui.theme.IBMPlexSans
import com.brainwallet.ui.theme.bentoEmojiSectionGradient
import com.brainwallet.ui.theme.bentoModalDarkGradient
import com.brainwallet.ui.theme.colorMidnite
import kotlinx.collections.immutable.ImmutableList
import kotlin.collections.toByteArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickEmojisScreen(
    firstThreeWords: ImmutableList<String>,
    modifier: Modifier = Modifier,
    onEmojisPicked: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    var selectedEmojis by remember { mutableStateOf(listOf<String?>(null, null, null)) }
    var activeSlot by remember { mutableStateOf<Int?>(null) }
    var shouldShowSeedWords by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(brush = bentoModalDarkGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(modifier = Modifier.height(44.dp)) {
                IconButton(
                    onClick = { onEmojisPicked(false) },
                ) {
                    Icon(
                        painter = painterResource(id = com.brainwallet.R.drawable.btn_clear),
                        contentDescription = stringResource(R.string.clear),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }

            Row {
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(start = 30.dp, end = 30.dp)
                        .padding(bottom = 10.dp),
                    text = stringResource(R.string.pick_emojis_title),
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        fontSize = 22.sp,
                        color = Color.White
                    ),
                    maxLines = 2
                )
            }

            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(start = 30.dp, end = 30.dp)
                    .padding(top = 5.dp, bottom = 5.dp),
                text = stringResource(R.string.pick_emojis_description),
                style = TextStyle(
                    fontFamily = IBMPlexSans,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    color = Color.White
                ),
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3 emoji picker slots
            val sectionSize = 120.dp

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(sectionSize + 32.dp)
                    .padding(6.dp)
                    .clip(DesignTheme.shapes.large)
                    .background(brush = bentoEmojiSectionGradient)
                    .border(
                        width = 0.3.dp,
                        color = Color.White.copy(alpha = 0.1f),
                        shape = DesignTheme.shapes.large
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    selectedEmojis.forEachIndexed { index, emoji ->
                        EmojiSlot(
                            emoji = emoji,
                            word = firstThreeWords[index],
                            index = index,
                            shouldShowWord = shouldShowSeedWords,
                            isActive = activeSlot == index,
                            onClick = { activeSlot = index },
                            modifier = Modifier
                                .width(sectionSize)
                                .height(sectionSize)
                                .padding(horizontal = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Icon(
                modifier = Modifier.align(Alignment.CenterHorizontally)
                    .padding(bottom = 5.dp)
                    .size(30.dp)
                    .clickable {
                        shouldShowSeedWords = !shouldShowSeedWords
                    },
                painter = if (shouldShowSeedWords) {
                    painterResource(id = R.drawable.visibility_off)
                } else {
                    painterResource(id = R.drawable.visibility_svg)
                },
                contentDescription = "Toggle Seed words show",
                tint = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
                    .padding(start = 16.dp, end = 16.dp)
                    .clip(DesignTheme.shapes.large)
                    .background(brush = bentoEmojiSectionGradient)
                    .border(
                        width = 0.5.dp,
                        color = Color.White.copy(alpha = 0.2f),
                        shape = DesignTheme.shapes.large
                    )
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    EmojiPickerSection(
                        modifier = Modifier.fillMaxSize(),
                        onEmojiSelected = { emoji ->
                            activeSlot?.let { slot ->
                                selectedEmojis = selectedEmojis
                                    .toMutableList().apply {
                                        this[slot] = emoji
                                        shouldShowSeedWords = !shouldShowSeedWords
                                    }
                                activeSlot = null
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            FilledTonalButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                    ),
                enabled = true,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color.White,
                    contentColor = if (selectedEmojis.all { it != null }) {
                        colorMidnite
                    } else {
                        colorMidnite.copy(
                            alpha = 0.3f
                        )
                    }
                ),
                onClick = {
                    if (selectedEmojis.all { it != null }) {
                        val joined = selectedEmojis.joinToString(separator = "")
                        val bytes = joined.toByteArray(Charsets.UTF_8)
                        BRKeyStore.putEmojis(
                            bytes,
                            context,
                            BWConstants.PUT_EMOJIS_REQUEST_CODE
                        )
                        BRSharedPrefs.putEmojisChosen(context, true)
                        onEmojisPicked(true)
                        AnalyticsManager
                            .logCustomAdHocEvent("user_set_first_set_emojis", null)
                    }
                },
                shape = RoundedCornerShape(11.dp)

            ) {
                Text(
                    modifier = Modifier,
                    text = if (selectedEmojis.all { it != null }) {
                        stringResource(R.string.lets_play_label)
                    } else {
                        stringResource(R.string.pick_emojis_title)
                    },
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                )
            }
            Spacer(modifier = Modifier.weight(0.05f))
        }
    }
}

@Composable
fun EmojiPickerSection(
    modifier: Modifier = Modifier,
    onEmojiSelected: (String) -> Unit = {},
    emojiIndex: Int = 0
) {
    AndroidView(
        factory = { context ->
            EmojiPickerView(context).apply {
                setOnEmojiPickedListener { emojiViewItem ->
                    onEmojiSelected(emojiViewItem.emoji)
                }
            }
        },
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(rememberNestedScrollInteropConnection()),
    )
}

@Composable
private fun EmojiSlot(
    emoji: String?,
    word: String?,
    index: Int,
    shouldShowWord: Boolean,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(
                if (isActive) {
                    1.dp
                } else {
                    0.3.dp
                },
                if (isActive) {
                    Color.White
                } else {
                    Color.White.copy(alpha = 0.1f)
                },
                RoundedCornerShape(12.dp)
            )
            .background(brush = bentoEmojiSectionGradient)
            .clickable { onClick.invoke() }
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier,
            contentAlignment = Alignment.Center
        ) {
            if (shouldShowWord) {
                if (word != null) {
                    Box {
                        val number = index + 1
                        Text(
                            modifier = Modifier
                                .padding(horizontal = 8.dp, vertical = 3.dp),
                            text = number.toString(),
                            style = TextStyle(
                                fontFamily = IBMPlexSans,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Start,
                                fontSize = 9.sp,
                                color = Color.White.copy(alpha = 0.6f)
                            ),
                        )
                        Text(
                            modifier = Modifier
                                .clip(RoundedCornerShape(percent = 50))
                                .background(Color.White.copy(alpha = 0.08f))
                                .padding(horizontal = 20.dp, vertical = 5.dp),
                            text = word,
                            style = TextStyle(
                                fontFamily = IBMPlexSans,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                fontSize = 16.sp,
                                color = Color.White
                            ),
                        )
                    }
                }
            } else {
                if (emoji != null) {
                    Text(
                        text = emoji,
                        fontSize = 48.sp,
                    )
                } else {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.sentiment_satisfied_24px),
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}
