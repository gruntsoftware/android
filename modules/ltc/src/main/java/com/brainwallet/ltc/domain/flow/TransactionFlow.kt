package com.brainwallet.ltc.domain.flow

import com.brainwallet.ltc.domain.model.TxItem
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
interface TransactionFlow : StateFlow<List<TxItem>> {
    fun refresh()
}
