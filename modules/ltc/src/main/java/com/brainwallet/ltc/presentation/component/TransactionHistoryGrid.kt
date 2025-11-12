package com.brainwallet.ltc.presentation.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brainwallet.design.presentation.component.effect.CardOpacityContainer
import com.brainwallet.design.presentation.component.effect.OpacityContainer
import com.brainwallet.ltc.R
import com.brainwallet.ltc.domain.flow.TransactionFlow
import com.brainwallet.ltc.domain.model.TxItem
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme
import com.grunt.brainwallet.core.presentation.util.toLtcStringFormatted
import com.grunt.brainwallet.iap.presentation.model.ExportedTransaction
import com.grunt.brainwallet.iap.presentation.screen.ExportTrxSheet
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.brainwallet.design.R as DesignR

@Stable
class TransactionHistoryGridUiState(
    initialTransactions: PersistentList<TxItem> = persistentListOf()
) {
    var showExportDialog by mutableStateOf(false)
        private set

    var transactions by mutableStateOf(initialTransactions)
        private set

    val exportedTransaction get() = mapToExportedTransactions(transactions)

    fun toggleShowExport() {
        showExportDialog = !showExportDialog
    }

    fun updateTransactions(newTransactions: PersistentList<TxItem>) {
        transactions = newTransactions
    }

    private fun mapToExportedTransactions(txItems: List<TxItem>): PersistentList<ExportedTransaction> {
        return txItems.map {
            ExportedTransaction(
                timeStamp = it.timeStamp,
                blockHeight = it.blockHeight,
                txHashReversed = it.txReversed,
                sent = it.sent,
                received = it.received,
                fee = it.fee,
                to = it.to.toList()
            )
        }.toPersistentList()
    }
}

@Composable
fun rememberTransactionHistoryGridState(
    transactionFlow: TransactionFlow = koinInject()
): TransactionHistoryGridUiState {
    val transactions by transactionFlow.collectAsStateWithLifecycle()
    val state = remember { TransactionHistoryGridUiState() }
    LaunchedEffect(transactions) {
        state.updateTransactions(transactions.toPersistentList())
    }
    return state
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryGrid(
    modifier: Modifier = Modifier,
    uiState: TransactionHistoryGridUiState = rememberTransactionHistoryGridState(),
    onTransactionClick: (TxItem) -> Unit = {}
) {
    val transactions = uiState.transactions

    CardOpacityContainer(
        modifier = modifier.fillMaxWidth()
    ) {
        AnimatedVisibility(
            uiState.transactions.isNotEmpty(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            OpacityContainer(
                shape = CircleShape,
                modifier = Modifier.clickable {
                    uiState.toggleShowExport()
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.launch),
                    contentDescription = "Export",
                    modifier = Modifier
                        .size(32.dp)
                        .padding(6.dp),
                    tint = BrainwalletTheme.colors.content
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 20.dp),
        ) {
            Text(
                text = "TRANSACTION HISTORY",
                style = BrainwalletTheme.typography.titleMedium.copy(
                    color = BrainwalletTheme.colors.content,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (transactions.isEmpty()) {
                Text(
                    text = "No transactions yet",
                    style = BrainwalletTheme.typography.bodyMedium.copy(
                        color = BrainwalletTheme.colors.content.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Normal
                    ),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            } else {
                val lazyListState = rememberLazyListState()
                val snapFlingBehavior = rememberSnapFlingBehavior(lazyListState = lazyListState)

                LazyColumn(
                    state = lazyListState,
                    flingBehavior = snapFlingBehavior,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(transactions) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onClick = { onTransactionClick(transaction) }
                        )
                    }
                }
            }
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (uiState.showExportDialog) {
        ModalBottomSheet(
            sheetState = sheetState,
            containerColor = BrainwalletTheme.colors.surface,
            contentColor = BrainwalletTheme.colors.content,
            onDismissRequest = { uiState.toggleShowExport() }
        ) {
            ExportTrxSheet(
                transactions = uiState.exportedTransaction,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: TxItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val netAmount = transaction.received - transaction.sent
    val isReceived = netAmount > 0
    val amountText = buildString {
        append(if (isReceived) "+" else "-")
        append("Ł")
        append(kotlin.math.abs(netAmount).toLtcStringFormatted())
    }
    val amountColor = if (isReceived) {
        BrainwalletTheme.colors.affirm
    } else {
        BrainwalletTheme.colors.error
    }

    val dateFormatter = remember {
        SimpleDateFormat("MMMM dd, yyyy 'at' h:mm a", Locale.getDefault())
    }
    val formattedDate = remember(transaction.timeStamp) {
        dateFormatter.format(Date(transaction.timeStamp * 1000))
    }

    val addressText = transaction.from.firstOrNull { it.isNotEmpty() }?.let { address ->
        "from ${address.take(12)}...${address.takeLast(8)}"
    } ?: transaction.to.firstOrNull { it.isNotEmpty() }?.let { address ->
        "to ${address.take(12)}...${address.takeLast(8)}"
    } ?: "unknown"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp)
            .padding(start = 16.dp, end = 18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isReceived) {
                            BrainwalletTheme.colors.affirm.copy(alpha = 0.1f)
                        } else {
                            BrainwalletTheme.colors.error.copy(alpha = 0.1f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(
                        id = if (isReceived) {
                            DesignR.drawable.mtrl_ic_arrow_drop_up
                        } else {
                            DesignR.drawable.mtrl_ic_arrow_drop_down
                        }
                    ),
                    contentDescription = if (isReceived) "Received" else "Sent",
                    tint = if (isReceived) {
                        BrainwalletTheme.colors.affirm
                    } else {
                        BrainwalletTheme.colors.error
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = formattedDate,
                    style = BrainwalletTheme.typography.bodyMedium.copy(
                        color = BrainwalletTheme.colors.content,
                        fontWeight = FontWeight.Medium
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = addressText,
                    style = BrainwalletTheme.typography.bodySmall.copy(
                        color = BrainwalletTheme.colors.content.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Normal
                    )
                )
            }
        }

        Text(
            text = amountText,
            style = BrainwalletTheme.typography.titleMedium.copy(
                color = amountColor,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
@PreviewLightDark
@Preview
fun TransactionHistoryGridEmptyPreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        TransactionHistoryGrid(
            modifier = Modifier.height(200.dp)
        )
    }
}

@Composable
@Preview
fun TransactionHistoryGridWithDataPreview() {
    val mockTransactions = listOf(
        TxItem(
            timeStamp = 1730059740L,
            blockHeight = 257985534,
            txHash = byteArrayOf(),
            txReversed = "abc123",
            sent = 400000L,
            received = 0L,
            fee = 1000L,
            to = listOf("ltc1bdyfovbabkgh78y9gib"),
            from = listOf("ltc1sender123456789"),
            balanceAfterTx = 1000000L,
            txSize = 250,
            outAmounts = listOf(400000L),
            isValid = true
        ),
        TxItem(
            timeStamp = 1729959740L,
            blockHeight = 257985533,
            txHash = byteArrayOf(),
            txReversed = "def456",
            sent = 0L,
            received = 500000L,
            fee = 0L,
            to = listOf("ltc1myaddress123456789"),
            from = listOf("ltc1sender987654321"),
            balanceAfterTx = 1400000L,
            txSize = 220,
            outAmounts = listOf(500000L),
            isValid = true
        )
    )

    val mockFlow = object : TransactionFlow {
        private val flow = MutableStateFlow(mockTransactions)
        override val value: List<TxItem> get() = flow.value
        override val replayCache: List<List<TxItem>> get() = listOf(flow.value)
        override suspend fun collect(collector: kotlinx.coroutines.flow.FlowCollector<List<TxItem>>): Nothing {
            flow.collect(collector)
        }

        override fun refresh() {}
    }

    BrainwalletTheme(isSystemInDarkTheme()) {
        TransactionHistoryGrid(
            modifier = Modifier.height(400.dp),
            uiState = rememberTransactionHistoryGridState(mockFlow)
        )
    }
}
