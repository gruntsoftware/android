package com.brainwallet.ui.bentosections.transactionbento

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.coordinatorlayout.R
import com.brainwallet.constants.BWConstants.EXPAND_DURATION
import com.brainwallet.constants.BWConstants.SHRINK_DURATION
import com.brainwallet.constants.transactionRowDetailHt
import com.brainwallet.constants.transactionRowHt
import com.brainwallet.presenter.entities.TxItem
import com.brainwallet.ui.screens.main.MainScreenEvent
import com.brainwallet.ui.screens.main.MainViewModel
import com.brainwallet.ui.theme.DesignTheme
import com.brainwallet.ui.theme.IBMPlexSans
import kotlinx.collections.immutable.ImmutableList
import org.koin.compose.viewmodel.koinViewModel
import timber.log.Timber

@Composable
fun TransactionsBentoScreen(
    transactions: ImmutableList<TxItem>,
    toggleState: TransactionFilterState,
    showTransactionDetail: Boolean,
    shouldShowFiatValues: Boolean,
    onEvent: (MainScreenEvent) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TransactionBentoViewModel = koinViewModel(),
    mainViewModel: MainViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    TransactionsBentoScreen(
        transactions = transactions,
        state = state,
        toggleState = toggleState,
        showTransactionDetail = showTransactionDetail,
        shouldShowFiatValues = shouldShowFiatValues,
        onEvent = onEvent,
        modifier = modifier,
        mainViewModel = mainViewModel
    )
}

@Composable
fun TransactionsBentoScreen(
    transactions: ImmutableList<TxItem>,
    state: TransactionBentoState,
    toggleState: TransactionFilterState,
    showTransactionDetail: Boolean,
    shouldShowFiatValues: Boolean,
    onEvent: (MainScreenEvent) -> Unit,
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = koinViewModel()
) {
    val listState = rememberLazyListState()
    val snappingLayout = remember(listState) { SnapLayoutInfoProvider(listState) }
    val flingBehavior = rememberSnapFlingBehavior(snappingLayout)

    val currentTransaction = remember(listState.firstVisibleItemIndex, transactions) {
        transactions.getOrNull(listState.firstVisibleItemIndex)
    }

    val toggleStateIcon = if (toggleState == TransactionFilterState.ALL) {
        painterResource(com.brainwallet.R.drawable.circle_circle_24dp)
    } else if (toggleState == TransactionFilterState.RECEIVED) {
        painterResource(com.brainwallet.R.drawable.ic_arrow_down)
    } else {
        painterResource(com.brainwallet.R.drawable.ic_arrow_up)
    }

    val toggleStateIconTint = if (toggleState == TransactionFilterState.ALL) {
        ColorFilter.tint(if (state.darkMode) Color.White else DesignTheme.colors.affirm)
    } else if (toggleState == TransactionFilterState.RECEIVED) {
        ColorFilter.tint(DesignTheme.colors.affirm)
    } else {
        ColorFilter.tint(DesignTheme.colors.error)
    }

    val noTxItemsPresent = remember(transactions) {
        transactions.isEmpty()
    }

    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            AnimatedVisibility(
                visible = showTransactionDetail,
                enter = expandVertically(tween(EXPAND_DURATION)),
                exit = shrinkVertically(animationSpec = tween(SHRINK_DURATION))
            ) {
                TransactionDetail(
                    isDarkMode = state.darkMode,
                    currentTransaction = currentTransaction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clipToBounds()
                )
            }
            Spacer(modifier = Modifier.weight(1f))

            if (noTxItemsPresent) {
                NoTxRow(
                    isDarkMode = state.darkMode,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(transactionRowHt)
                )
            } else {
                Box {
                    LazyColumn(
                        state = listState,
                        flingBehavior = flingBehavior,
                        contentPadding = if (showTransactionDetail) {
                            PaddingValues(
                                bottom = transactionRowHt
                            )
                        } else {
                            PaddingValues(bottom = 0.dp)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                if (showTransactionDetail) {
                                    transactionRowDetailHt
                                } else {
                                    transactionRowHt
                                }
                            )
                            .clip(RoundedCornerShape(16.dp))
                            .clipToBounds(),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        items(
                            items = transactions,
                            key = { it.id }
                        ) { transaction ->
                            TransactionRow(
                                isDarkMode = state.darkMode,
                                txItem = transaction,
                                ltcStats = state.ltcStats,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(transactionRowHt)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(0.22f)
                            .height(30.dp)
                            .align(Alignment.BottomStart)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = {
                                    onEvent(MainScreenEvent.OnToggleTransactionsFilter)
                                },
                            )
                            .padding(start = 12.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (state.darkMode) {
                                        Color.White.copy(alpha = 0.1f)
                                    } else {
                                        DesignTheme.colors.affirm.copy(alpha = 0.1f)
                                    }
                                )
                                .padding(6.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .wrapContentHeight(Alignment.CenterVertically)
                            ) {
                                Image(
                                    painter = toggleStateIcon,
                                    contentDescription = "circle_circle_icon",
                                    contentScale = ContentScale.Fit,
                                    colorFilter = toggleStateIconTint
                                )

                                Text(
                                    text = "${transactions.size} " +
                                        stringResource(com.brainwallet.R.string.txns_label),
                                    style = TextStyle(
                                        fontFamily = IBMPlexSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                    ),
                                    maxLines = 1,
                                    modifier = Modifier
                                        .padding(start = 4.dp),
                                    color = if (state.darkMode) Color.White else DesignTheme.colors.affirm
                                )

                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}
