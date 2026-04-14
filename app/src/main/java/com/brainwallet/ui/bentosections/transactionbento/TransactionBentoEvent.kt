package com.brainwallet.ui.bentosections.transactionbento

sealed class TransactionBentoEvent {
    data object OnLoad : TransactionBentoEvent()
    data class ToggleTransactionViews(val shouldShowDetail: Boolean) : TransactionBentoEvent()
    data class ToggleTransactionFilter(val filterState: TransactionFilterState) : TransactionBentoEvent()
}
