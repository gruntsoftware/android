package com.brainwallet.presenter.history

import com.brainwallet.presenter.entities.TxItem

sealed class HistoryFragmentEvent {
    /** The wallet's transaction list changed; [items] is the full, current list. */
    data class OnTxUpdated(val items: List<TxItem>) : HistoryFragmentEvent()
}
