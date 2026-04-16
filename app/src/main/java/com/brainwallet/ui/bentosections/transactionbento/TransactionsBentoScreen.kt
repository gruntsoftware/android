package com.brainwallet.ui.bentosections.transactionbento

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brainwallet.R
import com.brainwallet.constants.BWConstants.EXPAND_DURATION
import com.brainwallet.constants.BWConstants.SHRINK_DURATION
import com.brainwallet.constants.bentoCornerRadius
import com.brainwallet.constants.bentoSpacer
import com.brainwallet.constants.transactionActionHt
import com.brainwallet.constants.transactionDetailHt
import com.brainwallet.constants.transactionRowHt
import com.brainwallet.constants.tripleBentoSpacer
import com.brainwallet.presenter.entities.TxItem
import com.brainwallet.ui.bentosections.transactionbento.subscreens.CopyTransactionsWidget
import com.brainwallet.ui.bentosections.transactionbento.subscreens.NoTxRow
import com.brainwallet.ui.bentosections.transactionbento.subscreens.TransactionDetail
import com.brainwallet.ui.bentosections.transactionbento.subscreens.TransactionFilterWidget
import com.brainwallet.ui.bentosections.transactionbento.subscreens.TransactionRow
import com.brainwallet.ui.composable.screenHeightPercent
import com.brainwallet.ui.screens.main.MainScreenEvent
import com.brainwallet.ui.screens.main.MainViewModel
import kotlinx.collections.immutable.ImmutableList
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TransactionsBentoScreen(
    transactions: ImmutableList<TxItem>,
    toggleState: TransactionFilterState,
    showTransactionDetail: Boolean,
    shouldShowFiatValues: Boolean,
    onBentoTap: () -> Unit,
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
        onBentoTap = onBentoTap,
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
    onBentoTap: () -> Unit,
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
        painterResource(R.drawable.circle_circle_24dp)
    } else if (toggleState == TransactionFilterState.RECEIVED) {
        painterResource(R.drawable.ic_arrow_down)
    } else {
        painterResource(R.drawable.ic_arrow_up)
    }

    val noTxItemsPresent = remember(transactions) {
        transactions.isEmpty()
    }

    var currentIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .collect { currentIndex = it }
    }

    Box(
        modifier = modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onBentoTap
            )
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    TransactionDetail(
                        isDarkMode = state.darkMode,
                        currentTransaction = currentTransaction,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                transactionDetailHt
                            )
                            .clipToBounds()
                    )
                    Spacer(modifier = Modifier.padding(bentoSpacer))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(transactionActionHt)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(0.5f)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { mainViewModel.onEvent(MainScreenEvent.OnToggleTransactionsFilter) }
                        ) {
                            TransactionFilterWidget(
                                isDarkMode = state.darkMode,
                                toggleState = toggleState,
                                toggleStateIcon = toggleStateIcon,
                                transactions = transactions,
                                modifier = Modifier
                                    .width(220.dp)
                            )
                        }
                        Spacer(modifier = Modifier.padding(bentoSpacer))
                        Box(
                            modifier = Modifier
                                .weight(0.5f)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    currentTransaction?.let {
                                        mainViewModel.onEvent(MainScreenEvent.OnCopyTransactions(it))
                                    }
                                }
                        ) {
                            CopyTransactionsWidget(
                                isDarkMode = state.darkMode,
                                modifier = Modifier.matchParentSize()
                            )
                        }
                    }
                }
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
                                top = tripleBentoSpacer,
                            )
                        } else {
                            PaddingValues(bottom = 0.dp)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(
                                if (showTransactionDetail) {
                                    screenHeightPercent(0.25f)
                                } else {
                                    transactionRowHt
                                }
                            )
                            .clip(RoundedCornerShape(bentoCornerRadius))
                            .clipToBounds(),
                        verticalArrangement = Arrangement.spacedBy(bentoSpacer),
                    ) {
                        itemsIndexed(
                            items = transactions,
                            key = { _, transaction: TxItem -> transaction.id }
                        ) { index, transaction: TxItem ->
                            TransactionRow(
                                isDarkMode = state.darkMode,
                                txItem = transaction,
                                ltcStats = state.ltcStats,
                                isCurrentItem = index == currentIndex,
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
}
