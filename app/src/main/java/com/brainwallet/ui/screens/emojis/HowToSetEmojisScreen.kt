@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.brainwallet.ui.screens.emojis

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.R
import com.brainwallet.ui.theme.DesignTheme
import com.brainwallet.ui.theme.IBMPlexSans
import com.brainwallet.ui.theme.bentoHowToSectionGradient
import com.brainwallet.ui.theme.bentoModalDarkGradient
import com.brainwallet.ui.theme.colorMidnite

@Composable
fun HowToSetEmojisScreen(
    modifier: Modifier = Modifier,
    onNextPage: () -> Unit = {}
) {
    val horizontalVerticalSpacing = 8

    val iconSizeBox = 85
    val iconSize = 30
    var resizedDescriptlineFontSize by remember { mutableStateOf(15.sp) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth()
            .background(brush = bentoModalDarkGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(15.dp)
                .background(
                    color = Color.Transparent,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(horizontalVerticalSpacing.dp),
        ) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(10.dp),
                text = stringResource(R.string.how_to_choose_emojis_title),
                style = TextStyle(
                    fontFamily = IBMPlexSans,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    fontSize = 22.sp,
                    color = Color.White
                ),
                maxLines = 2
            )

            Text(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(15.dp),
                text = stringResource(R.string.how_to_choose_emojis_description),
                style = TextStyle(
                    fontFamily = IBMPlexSans,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    color = Color.White
                ),
                maxLines = 2
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth()
                    .fillMaxHeight(0.65f)
                    .clip(DesignTheme.shapes.large)
                    .background(
                        brush = bentoHowToSectionGradient,
                    )
                    .border(
                        width = 0.3.dp,
                        color = Color.White.copy(alpha = 0.1f),
                        shape = DesignTheme.shapes.large
                    ),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly

                ) {
                    Row(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .size(iconSizeBox.dp)
                                .padding(10.dp)
                                .padding(start = 10.dp)
                                .padding(top = 10.dp, bottom = 10.dp)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .border(
                                    0.3.dp,
                                    Color.White,
                                    CircleShape
                                )
                                .background(Color.White.copy(0.1f))
                        ) {
                            Icon(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(iconSize.dp),
                                tint = Color.White,
                                painter = painterResource(R.drawable.sentiment_satisfied_24px),
                                contentDescription = stringResource(id = R.string.ok)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(end = 20.dp)
                        ) {
                            Text(
                                modifier = Modifier
                                    .align(Alignment.Start)
                                    .padding(start = 10.dp, top = 5.dp, bottom = 5.dp),
                                text = stringResource(R.string.how_to_choose_emojis_bullet_one_title),
                                style = TextStyle(
                                    fontFamily = IBMPlexSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 17.sp,
                                    color = Color.White
                                ),
                                maxLines = 1
                            )

                            Text(
                                modifier = Modifier
                                    .align(Alignment.Start)
                                    .padding(start = 10.dp, top = 5.dp, bottom = 10.dp, end = 10.dp),
                                text = stringResource(R.string.how_to_choose_emojis_bullet_one_description),
                                onTextLayout = { textLayoutResult ->
                                    if (textLayoutResult.hasVisualOverflow) {
                                        resizedDescriptlineFontSize *= 0.95f
                                    }
                                },
                                style = TextStyle(
                                    fontFamily = IBMPlexSans,
                                    fontWeight = FontWeight.Light,
                                    fontSize = resizedDescriptlineFontSize,
                                    color = Color.White
                                ),
                                maxLines = 3
                            )
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier
                            .padding(start = 20.dp, end = 20.dp),
                        thickness = 0.5.dp,
                        Color.White.copy(0.2f)
                    )
                    Row(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .size(iconSizeBox.dp)
                                .padding(10.dp)
                                .padding(start = 10.dp)
                                .padding(top = 10.dp, bottom = 10.dp)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .border(
                                    0.3.dp,
                                    Color.White,
                                    CircleShape
                                )
                                .background(Color.White.copy(0.1f))
                        ) {
                            Icon(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(iconSize.dp),
                                tint = Color.White,
                                painter = painterResource(R.drawable.check_box_24px),
                                contentDescription = stringResource(id = R.string.ok)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(end = 20.dp)
                        ) {
                            Text(
                                modifier = Modifier
                                    .align(Alignment.Start)
                                    .padding(start = 10.dp, top = 5.dp, bottom = 5.dp),
                                text = stringResource(R.string.how_to_choose_emojis_bullet_two_title),
                                style = TextStyle(
                                    fontFamily = IBMPlexSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 17.sp,
                                    color = Color.White
                                ),
                                maxLines = 1
                            )

                            Text(
                                modifier = Modifier
                                    .align(Alignment.Start)
                                    .padding(start = 10.dp, top = 5.dp, bottom = 10.dp, end = 10.dp),
                                text = stringResource(R.string.how_to_choose_emojis_bullet_two_description),
                                onTextLayout = { textLayoutResult ->
                                    if (textLayoutResult.hasVisualOverflow) {
                                        resizedDescriptlineFontSize *= 0.95f
                                    }
                                },
                                style = TextStyle(
                                    fontFamily = IBMPlexSans,
                                    fontWeight = FontWeight.Light,
                                    fontSize = resizedDescriptlineFontSize,
                                    color = Color.White
                                ),
                                maxLines = 3
                            )
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier
                            .padding(start = 20.dp, end = 20.dp),
                        thickness = 0.5.dp,
                        Color.White.copy(0.2f)
                    )
                    Row(
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .size(iconSizeBox.dp)
                                .padding(10.dp)
                                .padding(start = 10.dp)
                                .padding(top = 10.dp, bottom = 10.dp)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .border(
                                    0.3.dp,
                                    Color.White,
                                    CircleShape
                                )
                                .background(Color.White.copy(0.1f))
                        ) {
                            Icon(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(iconSize.dp),
                                tint = Color.White,
                                painter = painterResource(R.drawable.lock_24px),
                                contentDescription = stringResource(id = R.string.settings_title_lock)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .padding(end = 20.dp)
                        ) {
                            Text(
                                modifier = Modifier
                                    .align(Alignment.Start)
                                    .padding(start = 10.dp, top = 5.dp, bottom = 5.dp),
                                text = stringResource(R.string.how_to_choose_emojis_bullet_three_title),
                                style = TextStyle(
                                    fontFamily = IBMPlexSans,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 17.sp,
                                    color = Color.White
                                ),
                                maxLines = 1
                            )

                            Text(
                                modifier = Modifier
                                    .align(Alignment.Start)
                                    .padding(start = 10.dp, top = 5.dp, bottom = 10.dp, end = 10.dp),
                                text = stringResource(R.string.how_to_choose_emojis_bullet_three_description),
                                style = TextStyle(
                                    fontFamily = IBMPlexSans,
                                    fontWeight = FontWeight.Light,
                                    fontSize = resizedDescriptlineFontSize,
                                    color = Color.White
                                ),
                                maxLines = 3
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(0.9f))

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
                    contentColor = colorMidnite
                ),
                onClick = {
                    onNextPage()
                },
                shape = RoundedCornerShape(11.dp)

            ) {
                Text(
                    modifier = Modifier,
                    text = stringResource(R.string.RecoverWallet_next),
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                )
            }
            Spacer(modifier = Modifier.weight(0.2f))
        }
    }
}
