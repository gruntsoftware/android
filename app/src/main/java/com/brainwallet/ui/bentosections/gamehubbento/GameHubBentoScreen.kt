package com.brainwallet.ui.bentosections.gamehubbento

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.R
import com.brainwallet.constants.BWConstants.FADE_IN_DURATION
import com.brainwallet.constants.BWConstants.FADE_OUT_DURATION
import com.brainwallet.constants.bentoCornerRadius
import com.brainwallet.game.contract.LocalGameSlot
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.ui.screens.gamehub.GameHubEvent
import com.brainwallet.ui.screens.gamehub.GameHubViewModel
import com.brainwallet.ui.theme.IBMPlexSans
import com.brainwallet.ui.theme.LilitaOne
import com.brainwallet.ui.theme.gameTaglineGradient
import com.brainwallet.ui.theme.gameTitleGradient
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GameHubBentoScreen(
    isGameHubOpen: Boolean,
    modifier: Modifier = Modifier,
    onToggle: () -> Unit = {},
    viewModel: GameHubViewModel = koinViewModel()
) {
    val gameHubBackground = R.drawable.game_hub_bk
    var resizedTaglineFontSize by remember { mutableStateOf(14.sp) }
    var slot = LocalGameSlot.current
    val context = LocalContext.current
    val address = BRSharedPrefs.getReceiveAddress(context)
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        if (isGameHubOpen) {
            Box(Modifier.fillMaxSize()) {
                slot?.Render(
                    Modifier.fillMaxSize(),
                    visible = isGameHubOpen,
                    launchParams = address,
                    onExit = { bytes: ByteArray? ->
                        bytes?.let {
                            val json = it.decodeToString()
                            viewModel.onEvent(GameHubEvent.OnGameExited(json, endData = ))
                        } ?: viewModel.onEvent(GameHubEvent.OnGameExited(null, endData = null))
                    }
                )
            }
        }
        AnimatedVisibility(
            visible = !isGameHubOpen,
            enter = fadeIn(tween(FADE_IN_DURATION)),
            exit = fadeOut(tween(FADE_OUT_DURATION)),
        ) {
            Image(
                painter = painterResource(gameHubBackground),
                contentDescription = "game_hub_background",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(bentoCornerRadius)),
                contentScale = ContentScale.Crop,
            )
            val textWidthRatio = 0.85f
            FallinScene(
                modifier = Modifier
                    .fillMaxWidth(textWidthRatio)
                    .fillMaxHeight(1f)
                    .align(Alignment.CenterEnd)
                    .pointerInput(Unit) {},
                dotQuantity = 14
            )
            Column(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(textWidthRatio)
                        .height(44.dp)
                ) {
                    Text(
                        text = "FALLINMOJI",
                        style = TextStyle(
                            fontFamily = LilitaOne,
                            fontWeight = FontWeight.Normal,
                            fontSize = 39.sp,
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                offset = Offset(x = 4f, y = 4f),
                                blurRadius = 4f
                            )
                        ),
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth(textWidthRatio)
                    )
                    Text(
                        text = "FALLINMOJI",
                        style = TextStyle(
                            brush = gameTitleGradient,
                            fontFamily = LilitaOne,
                            fontWeight = FontWeight.Normal,
                            fontSize = 39.sp
                        ),
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth(textWidthRatio)
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth(textWidthRatio)
                        .padding(top = 1.dp)
                ) {
                    Text(
                        text = stringResource(R.string.game_hub_tagline),
                        onTextLayout = { textLayoutResult ->
                            if (textLayoutResult.hasVisualOverflow) {
                                resizedTaglineFontSize *= 0.95f
                            }
                        },
                        style = TextStyle(
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = resizedTaglineFontSize,
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                offset = Offset(x = 4f, y = 4f),
                                blurRadius = 4f
                            )
                        ),
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth(textWidthRatio)
                    )
                    Text(
                        text = stringResource(R.string.game_hub_tagline),
                        onTextLayout = { textLayoutResult ->
                            if (textLayoutResult.hasVisualOverflow) {
                                resizedTaglineFontSize *= 0.95f
                            }
                        },
                        style = TextStyle(
                            brush = gameTaglineGradient,
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.Medium,
                            fontSize = resizedTaglineFontSize
                        ),
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth(textWidthRatio)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = stringResource(R.string.game_hub_title).uppercase(),
                    color = Color.White,
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 10.sp
                    ),
                    modifier = Modifier
                        .padding(start = 1.dp, end = 1.dp)
                        .background(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 2.dp)
                )
            }
        }
    }
}
