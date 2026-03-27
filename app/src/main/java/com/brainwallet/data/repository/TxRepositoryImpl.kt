package com.brainwallet.data.repository

import android.app.Application
import com.brainwallet.presenter.entities.TxItem
import com.brainwallet.wallet.BRWalletManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import kotlin.collections.orEmpty

@Single(binds = [TxRepository::class])
class TxRepositoryImpl(
    private val app: Application
) : TxRepository {

    private val _transactionItems = MutableStateFlow<List<TxItem>>(emptyList())
    override val transactionItems: StateFlow<List<TxItem>> = _transactionItems.asStateFlow()

    init {
        BRWalletManager.getInstance().addBalanceChangedListener { _ ->
            val items = BRWalletManager.getInstance().getTransactions()?.toList().orEmpty()
            _transactionItems.update { items }
        }
    }

    override suspend fun refresh() = withContext(Dispatchers.IO) {
        val items = BRWalletManager.getInstance().getTransactions()?.toList().orEmpty()
        _transactionItems.update { items }
    }
}
