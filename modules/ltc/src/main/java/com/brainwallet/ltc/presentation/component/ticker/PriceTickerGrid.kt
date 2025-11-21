package com.brainwallet.ltc.presentation.component.ticker

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.design.presentation.component.effect.CardOpacityContainer
import com.brainwallet.ltc.domain.model.TradingPairData
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme
import kotlinx.collections.immutable.persistentListOf

@Composable
fun PriceTickerGrid(
    modifier: Modifier = Modifier,
    uiState: PriceTickerGridUiState = rememberPriceTickerGridState(),
    onClick: () -> Unit = {}
) {
    val currentPair = uiState.currentTradingPair

    CardOpacityContainer(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (currentPair != null) {
                Text(
                    text = currentPair.pairSymbol,
                    style = BrainwalletTheme.typography.bodyMedium.copy(
                        color = BrainwalletTheme.colors.content.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    ),
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = currentPair.formattedPrice,
                    style = BrainwalletTheme.typography.headlineMedium.copy(
                        color = BrainwalletTheme.colors.content,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
                if (uiState.formattedLastSyncTime.isNotEmpty()) {
                    Text(
                        text = uiState.formattedLastSyncTime,
                        style = BrainwalletTheme.typography.bodySmall.copy(
                            color = BrainwalletTheme.colors.content.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp
                        ),
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
@PreviewLightDark
@Preview
fun PriceTickerGridPreview() {
    BrainwalletTheme(darkTheme = isSystemInDarkTheme()) {
        PriceTickerGrid(
            modifier = Modifier.height(120.dp),
            uiState = PriceTickerGridUiState(
                tradingPairs = persistentListOf(
                    TradingPairData("LTC/USD", 115.96, "$115.96"),
                    TradingPairData("LTC/EUR", 108.45, "€108.45"),
                    TradingPairData("LTC/GBP", 92.18, "£92.18"),
                    TradingPairData("LTC/JPY", 17850.0, "¥17,850")
                ),
                currentCurrency = "USD",
                lastSyncTimestamp = System.currentTimeMillis()
            )
        )
    }
}
