package com.brainwallet.ui.bentosections.transactionbento

import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.LtcStats

data class TransactionBentoState(
    val darkMode: Boolean = true,
    val ltcStats: LtcStats? = null,
    val selectedCurrency: CurrencyEntity? = null
)
