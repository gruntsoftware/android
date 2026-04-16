package com.brainwallet.ui.screens.send

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.R
import com.brainwallet.ui.screens.main.MainViewModel
import com.brainwallet.ui.theme.IBMPlexSans
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PreSendAmountRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    viewModel: SendViewModel = koinViewModel(),
    mainViewModel: MainViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val mainState by mainViewModel.state.collectAsState()
    val sectionHeight = 68.dp
    val sectionBorder = 0.8.dp
    val sectionDarkColor = Color.White.copy(alpha = 0.3f)
    val sectionLightColor = Color.Black.copy(alpha = 0.95f)
    val onEvent = viewModel::onEvent

    val fieldTextStyle = TextStyle(
        fontFamily = IBMPlexSans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        textAlign = TextAlign.Start,
    )

    Box(
        modifier = Modifier
            .height(sectionHeight)
            .fillMaxSize()
            .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
            .border(
                width = sectionBorder,
                color = if (state.darkMode) sectionDarkColor else sectionLightColor,
                shape = RoundedCornerShape(16.dp)
            )
            .background(
                color = if (state.darkMode) Color.Transparent else Color.White,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                modifier = Modifier.padding(start = 16.dp),
                text = if (state.userViewsFiat) state.selectedCurrency.symbol else "Ł",
                textAlign = TextAlign.End,
                style = TextStyle(
                    fontFamily = IBMPlexSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = if (state.darkMode) Color.White else Color.Black
                ),
            )
            TextField(
                modifier = Modifier.height(sectionHeight)
                    .onFocusChanged { focusState ->
                        if (focusState.hasFocus) {
                            onEvent(SendEvent.OnFieldFocused)
                        }
                    },
                value = value,
                keyboardActions = KeyboardActions(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                singleLine = true,
                onValueChange = {
                    onEvent(SendEvent.OnAmountChanged(it))
                },
                textStyle = fieldTextStyle,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    cursorColor = if (state.darkMode) Color.White else Color.Black,
                    focusedTextColor = if (state.darkMode) {
                        if (state.isAmountBelowBalance) Color.White else Color.Red
                    } else {
                        Color.Black
                    },
                    unfocusedTextColor = if (state.darkMode) Color.White else Color.Black
                ),
                label = {
                    Text(label)
                }
            )
            Spacer(modifier = Modifier.weight(0.6f))
            Box(
                modifier = Modifier,
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(6.dp)
                        .background(
                            if (state.darkMode) {
                                Color.White.copy(0.15f)
                            } else {
                                Color.Black.copy(0.15f)
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                        .align(Alignment.Center)
                        .clickable {
                            onEvent(SendEvent.OnToggleFiatOrLTC)
                        }
                ) {
                    AnimatedContent(
                        targetState = state.userViewsFiat,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(400)) togetherWith
                                fadeOut(animationSpec = tween(400))
                        },
                        label = "fiatLtcToggle"
                    ) { shouldShowFiat ->
                        if (shouldShowFiat) {
                            Text(
                                modifier = Modifier
                                    .padding(8.dp),
                                text = mainState.selectedCurrency.symbol +
                                    " " + mainState.selectedCurrency.code,
                                style = TextStyle(
                                    fontFamily = IBMPlexSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    textAlign = TextAlign.Center,
                                    color = if (state.darkMode) Color.White else Color.Black
                                ),
                                maxLines = 1
                            )
                        } else {
                            Image(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                                    .align(alignment = Alignment.End),
                                painter = painterResource(
                                    if (state.darkMode) {
                                        R.drawable.white_ltc_coin
                                    } else {
                                        R.drawable.black_ltc_coin
                                    }
                                ),
                                contentDescription = "litecoin_coin",
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }
        }
    }
}
