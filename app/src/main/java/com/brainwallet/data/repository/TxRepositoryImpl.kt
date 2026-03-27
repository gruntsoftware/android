package com.brainwallet.data.repository

import android.app.Application
import com.brainwallet.presenter.entities.BWDatabaseTransactionEntity
import com.brainwallet.tools.sqlite.TransactionDataSource
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
    private val app: Application
) : TxRepository {

    private val _transactionItems = MutableStateFlow<List<BWDatabaseTransactionEntity>>(emptyList())
    override val transactionItems: StateFlow<List<BWDatabaseTransactionEntity>> = _transactionItems.asStateFlow()

    override suspend fun refresh() = withContext(Dispatchers.IO) {
        val items = BRWalletManager.getInstance().getTransactions()?.toList().orEmpty()
        Timber.d("TxRepositoryImpl refresh: count=${items.size}")
        _transactionItems.update { items }
    }
}
