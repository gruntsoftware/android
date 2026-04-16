package com.brainwallet.ui.bentosections.ltcpickerbento

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.R
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.data.model.GlobalCurrency
import com.brainwallet.ui.composable.VerticalWheelPicker
import com.brainwallet.ui.composable.WheelPickerFocusVertical
import com.brainwallet.ui.composable.rememberWheelPickerState
import com.brainwallet.ui.theme.IBMPlexSans
import com.brainwallet.ui.theme.bentoSurface
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LTCPickerBentoScreen(
    modifier: Modifier = Modifier,
    viewModel: LTCPickerBentoViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    LTCPickerBentoScreen(
        state = state,
        modifier = modifier
    )
}

@Composable
fun LTCPickerBentoScreen(
    state: LTCPickerBentoState,
    modifier: Modifier = Modifier,
    viewModel: LTCPickerBentoViewModel = koinViewModel()
) {
    val currenciesWithFlags = GlobalCurrency.entries.map {
        " " +
            it.countryFlag + " " + it.code + " / LTC"
    }

    val wheelPickerState = rememberWheelPickerState(initialIndex = 0)
    var resizedFiatFontSize by remember { mutableStateOf(16.sp) }
    var resizedCurrencyNameFontSize by remember { mutableStateOf(18.sp) }
    var resizedAsOfTimestampFontSize by remember { mutableStateOf(12.sp) }
    val context = LocalContext.current

    // Set the initial index to the selected fiat currency
    LaunchedEffect(Unit) {
        delay(500)
        wheelPickerState.scrollToIndex(state.getSelectedFiatRateIndex())
    }

    // Listen for changes in the selected fiat currency index
    LaunchedEffect(wheelPickerState) {
        snapshotFlow { wheelPickerState.currentIndex }
            .filter { it > -1 }
            .distinctUntilChanged()
            .debounce(700)
            .collect {
                viewModel.onEvent(LTCPickerBentoEvent.OnGlobalCurrencyChange(state.globalCurrencies[it]))
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .bentoSurface(state.darkMode)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                text = stringResource(com.brainwallet.R.string.ltc_price_label).uppercase(),
                color = if (state.darkMode) Color.White else Color.Black,
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
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
            Spacer(modifier = Modifier.weight(0.5f))
            VerticalWheelPicker(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .pointerInput(Unit) {
                        detectHorizontalDragGestures { change, _ ->
                            change.consume()
                        }
                    },
                count = currenciesWithFlags.size,
                state = wheelPickerState,
                unfocusedCount = 1,
                itemHeight = 30.dp,
                focus = {
                    WheelPickerFocusVertical(
                        dividerColor = if (state.darkMode) {
                            Color.White.copy(0.3f)
                        } else {
                            Color.Black.copy(alpha = 0.3f)
                        }
                    )
                }
            ) { index ->
                Text(
                    text = currenciesWithFlags[index],
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        color = if (state.darkMode) Color.White else Color.Black
                    )
                )
            }
            Text(
                text = "${state.selectedCurrency.name}",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                onTextLayout = { textLayoutResult ->
                    if (textLayoutResult.hasVisualOverflow) {
                        resizedCurrencyNameFontSize *= 0.95f
                    }
                },
                style = TextStyle(
                    fontFamily = IBMPlexSans,
                    fontWeight = FontWeight.Light,
                    fontSize = resizedCurrencyNameFontSize,
                    color = if (state.darkMode) Color.White else Color.Black
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = state.formattedFiat,
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                onTextLayout = { textLayoutResult ->
                    if (textLayoutResult.hasVisualOverflow) {
                        resizedFiatFontSize *= 0.95f
                    }
                },
                style = TextStyle(
                    fontFamily = IBMPlexSans,
                    fontWeight = FontWeight.Normal,
                    fontSize = resizedFiatFontSize,
                    color = if (state.darkMode) Color.White else Color.Black
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = stringResource(
                    com.brainwallet.R.string.ltc_ticker_as_of_timestamp,
                    state.formattedTimeStamp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp, bottom = 4.dp),
                onTextLayout = { textLayoutResult ->
                    if (textLayoutResult.hasVisualOverflow) {
                        resizedAsOfTimestampFontSize *= 0.95f
                    }
                },
                style = TextStyle(
                    fontFamily = IBMPlexSans,
                    fontWeight = FontWeight.Normal,
                    fontSize = resizedAsOfTimestampFontSize,
                    color = if (state.darkMode) Color.White else Color.Black,
                    textAlign = TextAlign.Start
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun LTCPickerBentoScreenPreview() {
    Box(modifier = Modifier.fillMaxHeight(0.7f).padding(16.dp)) {
        LTCPickerBentoScreen(state = LTCPickerBentoState())
    }
}
