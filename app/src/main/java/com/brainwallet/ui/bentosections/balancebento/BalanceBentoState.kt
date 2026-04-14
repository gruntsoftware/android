package com.brainwallet.ui.bentosections.balancebento

import com.brainwallet.data.model.LtcStats
import com.brainwallet.presenter.entities.TxItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class BalanceBentoState(
    val darkMode: Boolean = true,
    val fiatCode: String = "USD",
    val symbol: String = "$",
    val topMessage: String = "Syncing...",
    val lastTimeStamp: String = "",
    val syncProgress: Float = 0.5f,
    val currentBlockHeight: Int = 0,
    val balanceHidden: Boolean = true,
    val brainwalletIsSyncing: Boolean = true,
    val transactions: ImmutableList<TxItem> = persistentListOf(),
    val isInternetReachable: Boolean = true,
    val ltcStats: LtcStats? = null,
)
