package com.brainwallet.data.source

import com.brainwallet.presenter.entities.TxItem

interface TransactionSource {
    fun getTransactions(): Array<TxItem>?
}
