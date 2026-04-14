package com.brainwallet.data.repository

import com.brainwallet.presenter.entities.TxItem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.StateFlow

interface TxRepository {
    val transactionItems: StateFlow<ImmutableList<TxItem>>
    suspend fun refresh()
}
