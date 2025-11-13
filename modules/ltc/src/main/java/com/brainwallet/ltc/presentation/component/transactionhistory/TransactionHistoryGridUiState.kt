package com.brainwallet.ltc.presentation.component.transactionhistory

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brainwallet.ltc.domain.flow.TransactionFlow
import com.brainwallet.ltc.domain.model.TxItem
import com.grunt.brainwallet.iap.presentation.model.ExportedTransaction
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import org.koin.compose.koinInject
import kotlin.collections.toList

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
