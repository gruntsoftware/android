package com.brainwallet.data.repository

import com.brainwallet.presenter.entities.TxItem
import kotlinx.coroutines.flow.StateFlow

interface TxRepository {
    val transactionItems: StateFlow<List<TxItem>>
    suspend fun refresh()
}
