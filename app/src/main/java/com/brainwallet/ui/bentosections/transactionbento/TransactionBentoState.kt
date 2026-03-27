package com.brainwallet.ui.bentosections.transactionbento

import com.brainwallet.presenter.entities.TxItem

data class TransactionBentoState(
    val darkMode: Boolean = true,
    val filterState: TransactionFilterState = TransactionFilterState.ALL,
    val transactions: List<TxItem> = emptyList()
)
