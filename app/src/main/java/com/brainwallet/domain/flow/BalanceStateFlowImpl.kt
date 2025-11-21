package com.brainwallet.domain.flow

import android.content.Context
import com.brainwallet.ltc.domain.flow.BalanceStateFlow
import com.brainwallet.ltc.domain.model.BalanceState
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.util.BRCurrency
import com.brainwallet.tools.util.BRExchange
import com.brainwallet.wallet.BRWalletManager
import kotlinx.coroutines.ExperimentalForInheritanceCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single
import java.math.BigDecimal

@OptIn(ExperimentalForInheritanceCoroutinesApi::class)
@Single
class BalanceStateFlowImpl(
    private val context: Context,
    private val upstream: MutableStateFlow<BalanceState> = MutableStateFlow(
        BRSharedPrefs.getCatchedBalance(
            context
        ).let { value ->
            BalanceState(
                getFormattedLTC(context, value),
                getFormattedLTCAsCurrency(context, value),
                value
            )
        }
    )
) : BalanceStateFlow,
    StateFlow<BalanceState> by upstream,
    BRWalletManager.OnBalanceChanged,
    BRSharedPrefs.OnIsoChangedListener {

    init {
        BRWalletManager.getInstance().addBalanceChangedListener(this)
        BRSharedPrefs.addIsoChangedListener(this)
        refreshBalance()
    }

    override fun refreshBalance() {
        BRWalletManager.getInstance().refreshBalance(context)
    }

    override fun onBalanceChanged(balance: Long) {
        upstream.update {
            balance.let { value ->
                BalanceState(
                    getFormattedLTC(context, value),
                    getFormattedLTCAsCurrency(context, value),
                    value
                )
            }
        }
    }

    override fun onIsoChanged(iso: String?) {
        refreshBalance()
    }
}

private fun getFormattedLTC(context: Context, value: Long): String {
    return BRCurrency.getFormattedCurrencyString(
        context,
        "LTC",
        BRExchange.getLitecoinForLitoshis(context, BigDecimal(value))
    ) ?: "$value"
}

private fun getFormattedLTCAsCurrency(context: Context, value: Long): String {
    return BRCurrency.getFormattedCurrencyString(
        context,
        BRSharedPrefs.getIsoSymbol(context),
        BRExchange.getAmountFromLitoshis(
            context,
            BRSharedPrefs.getIsoSymbol(context),
            BigDecimal(value)
        )
    ) ?: "$value"
}
