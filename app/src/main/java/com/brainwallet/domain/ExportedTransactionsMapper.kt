package com.brainwallet.domain

import com.brainwallet.presenter.entities.TxItem
import com.grunt.brainwallet.iap.presentation.model.ExportedTransaction
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import org.koin.core.annotation.Factory

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
