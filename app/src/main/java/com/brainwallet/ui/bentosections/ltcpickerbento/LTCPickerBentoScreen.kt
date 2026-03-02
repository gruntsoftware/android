package com.brainwallet.ui.bentosections.ltcpickerbento

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
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
import com.brainwallet.ui.theme.bentoDarkBorderGradient
import com.brainwallet.ui.theme.bentoDarkSurfaceGradient
import com.brainwallet.ui.theme.bentoLightBorderGradient
import com.brainwallet.ui.theme.bentoLightSurfaceGradient
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LTCPickerBentoScreen(
    modifier: Modifier = Modifier,
    viewModel: LTCPickerBentoViewModel = koinInject()
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
    var resizedLTCFiatFontSize by remember { mutableStateOf(44.sp) }
    var resizedAsOfFontSize by remember { mutableStateOf(10.sp) }
    var resizedLocalizedPriceFontSize by remember { mutableStateOf(20.sp) }
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

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(
                    brush = if (state.darkMode) bentoDarkSurfaceGradient else bentoLightSurfaceGradient,
                )
                .border(
                    width = 0.7.dp,
                    brush = if (state.darkMode) bentoDarkBorderGradient else bentoLightBorderGradient,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
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
                Spacer(modifier = Modifier.weight(0.5f))
                Text(
                    text = "${state.selectedCurrency.name}",
                    modifier = Modifier
                        .fillMaxWidth(),
                    onTextLayout = { textLayoutResult ->
                        if (textLayoutResult.hasVisualOverflow) {
                            resizedLocalizedPriceFontSize *= 0.95f
                        }
                    },
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.Light,
                        fontSize = resizedLocalizedPriceFontSize,
                        color = if (state.darkMode) Color.White else Color.Black
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${state.selectedCurrency.symbol} ${"%6.2f".format(state.selectedCurrency.rate)}",
                    modifier = Modifier.fillMaxWidth()
                        .padding(top = 1.dp, bottom = 1.dp),
                    onTextLayout = { textLayoutResult ->
                        if (textLayoutResult.hasVisualOverflow) {
                            resizedLTCFiatFontSize *= 0.95f
                        }
                    },
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = resizedLTCFiatFontSize,
                        color = if (state.darkMode) Color.White else Color.Black
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = state.formattedTimeStamp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp),
                    onTextLayout = { textLayoutResult ->
                        if (textLayoutResult.hasVisualOverflow) {
                            resizedAsOfFontSize *= 0.95f
                        }
                    },
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.Light,
                        fontSize = resizedAsOfFontSize,
                        color = if (state.darkMode) Color.White else Color.Black,
                        textAlign = TextAlign.Start
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
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
