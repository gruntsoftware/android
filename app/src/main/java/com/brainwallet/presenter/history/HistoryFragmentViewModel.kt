package com.brainwallet.presenter.history

import com.brainwallet.presenter.entities.TxItem
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import com.grunt.brainwallet.core.presentation.util.BaseViewModel
import com.grunt.brainwallet.iap.presentation.model.ExportedTransaction
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Factory

@KoinViewModel
class HistoryFragmentViewModel(
    private val mapper: ExportedTransactionsMapper
) : BaseViewModel<PersistentList<ExportedTransaction>, List<TxItem>>(persistentListOf()) {

    fun updateTx(items: List<TxItem>?) {
        if (items == null) return
        intent {
            reduce { mapper(items) }
        }
    }
}

@Factory
class ExportedTransactionsMapper {

    operator fun invoke(txItems: List<TxItem>): PersistentList<ExportedTransaction> {
        return mapToExportedTransactions(txItems)
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
