package com.brainwallet.domain.flow

import android.content.Context
import com.brainwallet.ltc.domain.flow.BalanceStateFlow
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.wallet.BRWalletManager
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
@Single
class BalanceStateFlowImpl(
    private val context: Context,
    private val upstream: MutableStateFlow<Long> = MutableStateFlow(
        BRSharedPrefs.getCatchedBalance(
            context
        )
    )
) : BalanceStateFlow, StateFlow<Long> by upstream, BRWalletManager.OnBalanceChanged {

    init {
        BRWalletManager.getInstance().addBalanceChangedListener(this)
        refreshBalance()
    }

    override fun refreshBalance() {
        BRWalletManager.getInstance().refreshBalance(context)
    }

    override fun onBalanceChanged(balance: Long) {
        upstream.update { balance }
    }
}
