package com.brainwallet.ui.bentosections.balancebento

sealed class BalanceBentoEvent {
    data object OnLoad : BalanceBentoEvent()
    data object OnToggleBalanceVisibility : BalanceBentoEvent()
    data class OnUpdatedSyncProgress(val syncProgress: Float) : BalanceBentoEvent()
}
