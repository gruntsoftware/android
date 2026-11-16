package com.brainwallet.presenter.history

import com.brainwallet.domain.ExportedTransactionsMapper
import com.brainwallet.ui.BrainwalletViewModel
import com.grunt.brainwallet.iap.export.transactions.presentation.model.ExportedTransaction
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class HistoryFragmentViewModel(
    private val mapper: ExportedTransactionsMapper
) : BrainwalletViewModel<HistoryFragmentEvent>() {

    private val _state = MutableStateFlow<PersistentList<ExportedTransaction>>(persistentListOf())
    val state: StateFlow<PersistentList<ExportedTransaction>> = _state.asStateFlow()

    override fun onEvent(event: HistoryFragmentEvent) {
        when (event) {
            // Called from HistoryFragment's background executor; StateFlow is thread-safe.
            is HistoryFragmentEvent.OnTxUpdated -> _state.value = mapper(event.items)
        }
    }
}
