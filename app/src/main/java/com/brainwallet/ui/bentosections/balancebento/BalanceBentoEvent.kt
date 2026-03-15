package com.brainwallet.ui.bentosections.balancebento

import android.content.Context

sealed class BalanceBentoEvent {
    data class OnLoad(val context: Context) : BalanceBentoEvent()
    data object OnToggleBalanceVisibility : BalanceBentoEvent()

    data object OnUpdatedSyncProgress : BalanceBentoEvent()
}
