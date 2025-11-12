package com.brainwallet.ltc.presentation.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import com.brainwallet.design.presentation.component.widget.PaginationDot
import com.brainwallet.ltc.domain.model.TradingPairData
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme
import kotlinx.collections.immutable.persistentListOf

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PriceTickerGrid(
    modifier: Modifier = Modifier,
    uiState: PriceTickerGridUiState = rememberPriceTickerGridState(),
    onClick: () -> Unit = {}
) {
    val pagerState = rememberPagerState(pageCount = { uiState.tradingPairs.size })

    CardOpacityContainer(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f)
            ) { page ->
                val currentPair = uiState.tradingPairs[page]

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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

                    Spacer(modifier = Modifier.height(4.dp))

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
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                uiState.tradingPairs.forEachIndexed { index, _ ->
                    PaginationDot(
                        isActive = index == pagerState.currentPage,
                        modifier = Modifier.padding(horizontal = 3.dp)
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
                initialTradingPairs = persistentListOf(
                    TradingPairData("LTC/USD", 115.96, "$115.96"),
                    TradingPairData("LTC/EUR", 108.45, "€108.45"),
                    TradingPairData("LTC/GBP", 92.18, "£92.18"),
                    TradingPairData("LTC/JPY", 17850.0, "¥17,850")
                )
            )
        )
    }
}
