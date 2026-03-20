package com.brainwallet.ui.bentosections.balancebento

import android.app.Application
import android.icu.math.BigDecimal
import androidx.lifecycle.viewModelScope
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.repository.ConnectivityRepository
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.data.repository.SyncAnalyticsRepository
import com.brainwallet.data.repository.TxRepository
import com.brainwallet.data.source.PeerManagerSource
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.sqlite.TransactionDataSource
import com.brainwallet.tools.util.BRExchange.ONE_LITECOIN_OF_LITOSHIS
import com.brainwallet.ui.BrainwalletViewModel
import com.brainwallet.wallet.BRPeerManager
import com.brainwallet.wallet.BRPeerManager.syncProgress
import com.brainwallet.wallet.BRWalletManager
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
import java.util.Date

@KoinViewModel
class BalanceBentoViewModel(
    private val app: Application,
    private val txRepository: TxRepository,
    private val settingRepository: SettingRepository,
    private val syncRepository: SyncAnalyticsRepository,
    private val peerManagerSource: PeerManagerSource,
    private val connectivityRepository: ConnectivityRepository,
) : BrainwalletViewModel<BalanceBentoEvent>(),
    BRWalletManager.OnBalanceChanged,
    BRPeerManager.OnTxStatusUpdate,
    BRSharedPrefs.OnIsoChangedListener,
    TransactionDataSource.OnTxAddedListener {
    private val _state = MutableStateFlow(BalanceBentoState())
    val state: StateFlow<BalanceBentoState> = _state.asStateFlow()
    val formatter = java.text.SimpleDateFormat(
        "MMMM dd, yyyy h:mm:ss a",
        java.util.Locale.getDefault()
    )
    private val appSetting = settingRepository.settings
        .distinctUntilChanged()
        .onEach { setting ->
            _state.update {
                it.copy(
                    darkMode = setting.isDarkMode,
                    selectedCurrency = setting.currency,
                    fiatCode = setting.currency.code,
                    symbol = setting.currency.symbol,
                )
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            AppSetting()
        )

    init {
        BRWalletManager.getInstance().addBalanceChangedListener(this)
        BRPeerManager.getInstance().addStatusUpdateListener(this)
        BRSharedPrefs.addIsoChangedListener(this)
        TransactionDataSource.getInstance(app).addTxAddedListener(this)
        viewModelScope.launch {
            connectivityRepository.isConnected.collect { isInternetReachable ->
                _state.update { it.copy(isInternetReachable = isInternetReachable) }
                if (isInternetReachable) {
                    val progress = syncProgress(BRSharedPrefs.getStartHeight(app)).toFloat()
                    onEvent(BalanceBentoEvent.OnUpdatedSyncProgress(progress))
                }
            }
            refreshBalance()
            // Read initial sync progress immediately
            val progress = BRPeerManager.syncProgress(
                BRSharedPrefs.getStartHeight(app)
            ).toFloat()
            onEvent(BalanceBentoEvent.OnUpdatedSyncProgress(progress))
        }
    }

    override fun onCleared() {
        super.onCleared()
        BRWalletManager.getInstance().removeListener(this)
        BRPeerManager.getInstance().removeListener(this)
        BRSharedPrefs.removeListener(this)
        TransactionDataSource.getInstance(app).removeListener(this)
    }

    override fun onTxAdded() {
        viewModelScope.launch(Dispatchers.IO) {
            txRepository.updateTransactions { transactions ->
                _state.update {
                    it.copy(
                        transactions = transactions,
                        lastBlock = transactions.firstOrNull()?.let { tx ->
                            if (tx.blockHeight > it.currentBlockHeight) {
                                tx.blockHeight
                            } else {
                                0
                            }
                        } ?: it.lastBlock,
                        lastTimeStamp = transactions.firstOrNull()?.let { tx ->
                            formatter.format(Date(tx.timeStamp * 1000))
                        } ?: it.lastTimeStamp
                    )
                }
            }
        }
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
        val progress = BRPeerManager.syncProgress(BRSharedPrefs.getStartHeight(app)).toFloat()
        _state.update {
            it.copy(
                currentBlockHeight = peerManagerSource.getCurrentBlockHeight(),
                lastTimeStamp = formatter.format(
                    Date(peerManagerSource.getLastBlockTimestamp() * 1000)
                ),
            )
        }
        onEvent(BalanceBentoEvent.OnUpdatedSyncProgress(progress))
    }

    override fun onIsoChanged(iso: String) {
        refreshBalance()
    }
    private fun refreshBalance() {
        val currentBalance = BRWalletManager.getInstance().getBalance(app)
        _state.update {
            it.copy(
                ltcBalance = BigDecimal(currentBalance)
                    .divide(BigDecimal(ONE_LITECOIN_OF_LITOSHIS))
            )
        }
    }

    override fun onEvent(event: BalanceBentoEvent) {
        when (event) {
            is BalanceBentoEvent.OnLoad -> {
                val startHeight = BRSharedPrefs.getStartHeight(app)
                val progress = BRPeerManager.syncProgress(startHeight).toFloat()
                _state.update {
                    it.copy(
                        lastTimeStamp = formatter.format(java.util.Date()),
                        selectedCurrency = appSetting.value.currency,
                        fiatCode = appSetting.value.currency.code,
                        symbol = appSetting.value.currency.symbol,
                        syncProgress = progress,
                        brainwalletIsSyncing = progress <= 0.999f
                    )
                }
            }
            is BalanceBentoEvent.OnToggleBalanceVisibility -> {
                _state.update { it.copy(balanceHidden = !it.balanceHidden) }
            }

            is BalanceBentoEvent.OnUpdatedSyncProgress -> {
                _state.update {
                    it.copy(
                        lastTimeStamp = formatter.format(java.util.Date()),
                        syncProgress = event.syncProgress,
                        brainwalletIsSyncing = event.syncProgress <= 0.999f
                    )
                }
            }
        }
    }
}
