package com.brainwallet.ltc.domain.model

sealed class SyncState {
    data object Idle : SyncState()
    data class Started(val startTimestamp: Long, val lastBlockHeight: Int) : SyncState()
    data class Syncing(
        val progress: Double,
        val timeStamp: String,
        val currentBlockHeight: Int
    ) : SyncState()
    data object Synced : SyncState()
    data class Error(val error: Throwable) : SyncState()
}
