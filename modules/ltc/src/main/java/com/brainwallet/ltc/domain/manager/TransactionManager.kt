package com.brainwallet.ltc.domain.manager

import com.brainwallet.ltc.domain.model.PromptState
import com.brainwallet.ltc.domain.model.SyncState
import com.brainwallet.ltc.domain.model.TxItem
import kotlinx.coroutines.flow.StateFlow

interface TransactionManager {
    val transactions: StateFlow<List<TxItem>>
    val syncState: StateFlow<SyncState>
    val currentPrompt: StateFlow<PromptState?>
    fun initialize()
    suspend fun refreshTransactions()
    suspend fun updateSyncProgress()
    fun showNextPrompt()
    fun dismissPrompt()
    fun onPromptAction(promptState: PromptState)
}
