package com.brainwallet.data.repository

import android.app.Application
import com.brainwallet.data.source.TransactionSource
import com.brainwallet.presenter.entities.TxItem
import com.brainwallet.wallet.BRWalletManager
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import timber.log.Timber

@Single(binds = [TxRepository::class])
class TxRepositoryImpl(
    private val app: Application,
    private val transactionSource: TransactionSource = BRWalletManager.getInstance(),
) : TxRepository {

    private val _transactionItems = MutableStateFlow<ImmutableList<TxItem>>(persistentListOf())
    override val transactionItems: StateFlow<ImmutableList<TxItem>> = _transactionItems.asStateFlow()

    override suspend fun refresh() = withContext(Dispatchers.IO) {
        val items = transactionSource.getTransactions()
            ?.toImmutableList() ?: persistentListOf()
        Timber.d("TxRepositoryImpl refresh: count=${items.size}")
        _transactionItems.update { items }
    }

    suspend fun allReceiveTransactions(): ImmutableList<TxItem> {
        val receivedItems = _transactionItems.value.filter { it.received > 0 }
        return receivedItems.toImmutableList()
    }

    suspend fun allSentTransactions(): ImmutableList<TxItem> {
        val sentItems = _transactionItems.value.filter { it.sent > 0 }
        return sentItems.toImmutableList()
    }
}
