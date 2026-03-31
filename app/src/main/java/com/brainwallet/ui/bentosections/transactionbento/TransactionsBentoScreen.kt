package com.brainwallet.ui.bentosections.transactionbento

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.gestures.snapping.SnapLayoutInfoProvider
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.unit.dp
import com.brainwallet.constants.BWConstants.EXPAND_DURATION
import com.brainwallet.constants.BWConstants.SHRINK_DURATION
import com.brainwallet.constants.transactionRowDetailHt
import com.brainwallet.constants.transactionRowHt
import com.brainwallet.presenter.entities.TxItem
import com.brainwallet.ui.screens.main.MainViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TransactionsBentoScreen(
    transactions: ImmutableList<TxItem>,
    toggleState: TransactionFilterState,
    showTransactionDetail: Boolean,
    modifier: Modifier = Modifier,
    viewModel: TransactionBentoViewModel = koinViewModel(),
    mainViewModel: MainViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    val filteredTransactions = remember(transactions, toggleState) {
        if (toggleState == TransactionFilterState.RECEIVED) {
            transactions
                .filter { it.received > 0 }
                .toImmutableList()
        } else if (toggleState == TransactionFilterState.SENT) {
            transactions
                .filter { it.sent > 0 }
                .toImmutableList()
        } else {
            transactions
        }
    }

    TransactionsBentoScreen(
        transactions = transactions,
        state = state,
        toggleState = toggleState,
        showTransactionDetail = showTransactionDetail,
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
    modifier: Modifier = Modifier,
    mainViewModel: MainViewModel = koinViewModel()
) {
    val listState = rememberLazyListState()
    val snappingLayout = remember(listState) { SnapLayoutInfoProvider(listState) }
    val flingBehavior = rememberSnapFlingBehavior(snappingLayout)

    val toggleStateText = if (toggleState == TransactionFilterState.ALL) {
        "All: "
    } else if (toggleState == TransactionFilterState.RECEIVED) {
        "Received: "
    } else {
        "Sent: "
    }

    val currentTransaction = remember(listState.firstVisibleItemIndex, transactions) {
        transactions.getOrNull(listState.firstVisibleItemIndex)
    }

    val noTxItemsPresent = remember(transactions) {
        transactions.isEmpty()
    }

    Box(
        modifier = modifier
    ) {
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
                        .height(if (showTransactionDetail) transactionRowDetailHt else transactionRowHt)
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(transactionRowHt)
                        )
                    }
                }
            }
        }
    }
}
