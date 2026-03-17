package com.brainwallet.ui.bentosections.balancebento

import android.icu.math.BigDecimal
import androidx.lifecycle.viewModelScope
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.data.repository.TxRepository
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.ui.BrainwalletViewModel
import com.brainwallet.wallet.BRPeerManager
import com.brainwallet.wallet.BRWalletManager
import com.brainwallet.worker.CurrencyUpdateWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class BalanceBentoViewModel(
    private val settingRepository: SettingRepository,
    private val currencyUpdateWorker: CurrencyUpdateWorker,
    private val txRepository: TxRepository,
) : BrainwalletViewModel<BalanceBentoEvent>(),
    BRWalletManager.OnBalanceChanged,
    BRPeerManager.OnTxStatusUpdate,
    BRSharedPrefs.OnIsoChangedListener {
    private val _state = MutableStateFlow(BalanceBentoState())
    val state: StateFlow<BalanceBentoState> = _state.asStateFlow()
    val formatter = java.text.SimpleDateFormat(
        "MMMM dd, yyyy h:mm:ss a",
        java.util.Locale.getDefault()
    )

    init {
        BRWalletManager.getInstance().addBalanceChangedListener(this)
        BRPeerManager.getInstance().addStatusUpdateListener(this)
        BRSharedPrefs.addIsoChangedListener(this)
    }

    private val appSetting = settingRepository.settings
        .distinctUntilChanged()
        .onEach { setting ->
            _state.update {
                it.copy(
                    darkMode = setting.isDarkMode,
                    formattedTimeStamp = formatter.format(java.util.Date())
                )
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            AppSetting()
        )

    override fun onCleared() {
        super.onCleared()
        BRWalletManager.getInstance().removeListener(this)
        BRPeerManager.getInstance().removeListener(this)
        BRSharedPrefs.removeListener(this)
    }

    override fun onBalanceChanged(balance: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            txRepository.updateTransactions { transactions ->
                _state.update { it.copy(transactions = transactions) }
            }
        }
    }

    override fun onStatusUpdate() {
        viewModelScope.launch(Dispatchers.IO) {
            txRepository.updateTransactions { transactions ->
                _state.update { it.copy(transactions = transactions) }
            }
        }
    }

    override fun onIsoChanged(iso: String) {
        refreshBalance()
    }
    private fun refreshBalance(balance: Long? = null) {
        // /This needed to be refactored and debugged
        // / val currentBalance = balance ?: BRWalletManager.getInstance().getBalance(this)
        val fiatBalance = // convert using BRSharedPrefs iso + your existing conversion logic
            _state.update {
                it.copy(
                    ltcBalance = BigDecimal.ZERO
                )
            }
    }

    override fun onEvent(event: BalanceBentoEvent) {
        when (event) {
            is BalanceBentoEvent.OnLoad -> {
                _state.update { currentState ->
                    currentState.copy(
                        formattedTimeStamp = formatter.format(java.util.Date())
                    )
                }
            }
            is BalanceBentoEvent.OnToggleBalanceVisibility -> {
                _state.update { it.copy(balanceHidden = !it.balanceHidden) }
            }

            is BalanceBentoEvent.OnUpdatedSyncProgress -> {
                _state.update {
                    it.copy(formattedTimeStamp = it.formattedTimeStamp)
                    it.copy(syncProgress = it.syncProgress)
                }
            }
        }
    }
}
