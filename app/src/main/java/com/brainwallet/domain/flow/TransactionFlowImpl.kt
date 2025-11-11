package com.brainwallet.domain.flow

import android.content.Context
import com.brainwallet.domain.mapper.toKotlin
import com.brainwallet.ltc.domain.flow.TransactionFlow
import com.brainwallet.ltc.domain.model.TxItem
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.sqlite.TransactionDataSource
import com.brainwallet.wallet.BRPeerManager
import com.brainwallet.wallet.BRWalletManager
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single
import timber.log.Timber

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
@Single
class TransactionFlowImpl(
    context: Context,
    private val upstream: MutableStateFlow<List<TxItem>> = MutableStateFlow(emptyList())
) : TransactionFlow,
    StateFlow<List<TxItem>> by upstream,
    TransactionDataSource.OnTxAddedListener,
    BRWalletManager.OnBalanceChanged,
    BRPeerManager.OnTxStatusUpdate,
    BRSharedPrefs.OnIsoChangedListener {

    init {
        BRWalletManager.getInstance().addBalanceChangedListener(this)
        BRPeerManager.getInstance().addStatusUpdateListener(this)
        BRSharedPrefs.addIsoChangedListener(this)
        TransactionDataSource.getInstance(context).addTxAddedListener(this)
    }

    override fun refresh() {
        val startTime = System.currentTimeMillis()
        val txArray = BRWalletManager.getInstance().transactions
        val txList = txArray?.toList() ?: emptyList()

        val duration = System.currentTimeMillis() - startTime
        if (duration > 500) {
            Timber.d("refreshTransactions took: $duration ms")
        }
        upstream.update { txList.toKotlin() }
    }

    override fun onTxAdded() {
        refresh()
    }

    override fun onBalanceChanged(balance: Long) {
        refresh()
    }

    override fun onStatusUpdate() {
        refresh()
    }

    override fun onIsoChanged(iso: String?) {
        refresh()
    }
}
