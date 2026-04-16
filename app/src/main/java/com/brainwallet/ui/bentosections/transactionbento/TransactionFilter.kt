package com.brainwallet.ui.bentosections.transactionbento

import androidx.annotation.StringRes
import com.brainwallet.R

enum class TransactionFilterState(@StringRes val labelRes: Int) {
    ALL(R.string.all_label),
    RECEIVED(R.string.received_label),
    SENT(R.string.sent_label)
}
