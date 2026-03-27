package com.brainwallet.data.repository

import com.brainwallet.presenter.entities.BWDatabaseTransactionEntity
import kotlinx.coroutines.flow.StateFlow

interface TxRepository {
    val transactionItems: StateFlow<List<BWDatabaseTransactionEntity>>
    suspend fun refresh()
}
