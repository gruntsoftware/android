package com.brainwallet.presenter.history

import com.brainwallet.domain.ExportedTransactionsMapper
import com.brainwallet.presenter.entities.TxItem
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import com.grunt.brainwallet.core.presentation.util.BaseViewModel
import com.grunt.brainwallet.iap.export.transactions.presentation.model.ExportedTransaction
import org.koin.android.annotation.KoinViewModel

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
