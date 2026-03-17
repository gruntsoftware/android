package com.brainwallet.data.repository

import android.app.Application
import com.brainwallet.presenter.entities.TxItem
import com.brainwallet.tools.manager.TxManager
import org.koin.core.annotation.Single

@Single
class TxRepository(private val app: Application) {
    fun updateTransactions(onResult: (List<TxItem>) -> Unit) {
        TxManager.getInstance().updateTxList(app) { onResult(it.orEmpty()) }
    }
}
