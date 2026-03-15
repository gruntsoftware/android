package com.brainwallet.ui.bentosections.balancebento

data class BalanceBentoState(
    val darkMode: Boolean = true,
    val fiatCode: String = "USD",
    val symbol: String = "$",
    val topMessage: String = "Syncing...",
    val formattedTimeStamp: String = "Date: Mar 15, 2026 at 19:35",
    val syncProgress: Float = 0.5f,
    val currentBlockHeight: Int = 5,
    val lastBlock: Int = 5,
    val fiatBalance: Float = 0.0f,
    val ltcBalance: Float = 0.0f,
    val balanceHidden: Boolean = true,
    val brainwalletIsSyncing: Boolean = true
)
