package com.brainwallet.ui.bentosections.balancebento

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.brainwallet.BuildConfig
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.repository.ConnectivityRepository
import com.brainwallet.data.repository.SettingRepository
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import timber.log.Timber
import java.math.BigDecimal
import java.util.Date

@KoinViewModel
class BalanceBentoViewModel(
    private val app: Application,
    private val txRepository: TxRepository,
    private val settingRepository: SettingRepository,
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
        "MMM dd, yyyy hh:mm a",
        java.util.Locale.getDefault()
    )

    val latestLTCBlockHeight = BRSharedPrefs.getLiveLtcStats(app).currentBlockHeight

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
        viewModelScope.launch {
            // Runs immediately on launch
            val progress = BRPeerManager.syncProgress(
                BRSharedPrefs.getStartHeight(app)
            ).toFloat()
            onEvent(BalanceBentoEvent.OnUpdatedSyncProgress(progress))

            // Then starts collecting (blocks here)
            connectivityRepository.isConnected.collect { isInternetReachable ->
                _state.update { it.copy(isInternetReachable = isInternetReachable) }
                if (isInternetReachable) {
                    onEvent(BalanceBentoEvent.OnUpdatedSyncProgress(progress))
                }
            }
        }

        viewModelScope.launch {
            peerManagerSource.blockInfo.collect { blockInfo ->
                Timber.d("timber: blockInfo Height:  %d", blockInfo.blockHeight)

                _state.update {
                    it.copy(
                        currentBlockHeight = blockInfo.blockHeight,
                        lastTimeStamp = formatter.format(Date(blockInfo.timestamp * 1000L)),
                        syncProgress = blockInfo.blockHeight.toFloat() / latestLTCBlockHeight.toFloat()
                    )
                }
            }
        }

        viewModelScope.launch {
            txRepository.transactionItems.collect { currentTransactions ->
                _state.update {
                    it.copy(
                        transactions = currentTransactions
                    )
                }
            }
        }
    }

    fun onResume() {
        viewModelScope.launch(Dispatchers.IO) {
            var attempts = 0
            while (!BRWalletManager.getInstance().isCreated() && attempts < 20) {
                delay(250)
                attempts++
            }
            if (BRWalletManager.getInstance().isCreated()) {
                addObservers()
                val balance = BRSharedPrefs.getCachedBalance(app)
                _state.update {
                    it.copy(
                        ltcBalance = balance,
                        litoshiBalance = BigDecimal(balance)
                            .divide(BigDecimal(ONE_LITECOIN_OF_LITOSHIS)),
                    )
                }
                txRepository.refresh()
            } else {
                Timber.d("BalanceBentoViewModel: wallet not ready after waiting")
            }
        }
    }

    fun onPause() {
        removeObservers()
    }

    override fun onCleared() {
        super.onCleared()
        BRWalletManager.getInstance().removeListener(this)
        BRPeerManager.getInstance().removeListener(this)
        BRSharedPrefs.removeListener(this)
        TransactionDataSource.getInstance(app).removeListener(this)
    }
    private fun addObservers() {
        BRWalletManager.getInstance().addBalanceChangedListener(this)
        BRPeerManager.getInstance().addStatusUpdateListener(this)
        BRSharedPrefs.addIsoChangedListener(this)
        TransactionDataSource.getInstance(app).addTxAddedListener(this)
    }
    private fun removeObservers() {
        BRWalletManager.getInstance().removeListener(this)
        BRPeerManager.getInstance().removeListener(this)
        BRSharedPrefs.removeListener(this)
        TransactionDataSource.getInstance(app).removeListener(this)
    }

    override fun onBalanceChanged(balance: Long) {
        _state.update {
            it.copy(
                ltcBalance = balance
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            txRepository.refresh()
        }
    }

    override fun onStatusUpdate() {
        viewModelScope.launch(Dispatchers.IO) {
            txRepository.refresh()
            val progress = peerManagerSource.getCurrentBlockHeight() / latestLTCBlockHeight.toFloat()
            _state.update {
                it.copy(
                    currentBlockHeight = peerManagerSource.getCurrentBlockHeight()
                )
            }
            onEvent(BalanceBentoEvent.OnUpdatedSyncProgress(progress))
        }
    }

    override fun onIsoChanged(iso: String) {
        viewModelScope.launch(Dispatchers.IO) {
            txRepository.refresh()
        }
    }

    override fun onTxAdded() {
        viewModelScope.launch(Dispatchers.IO) {
            txRepository.refresh()
        }
    }

    override fun onEvent(event: BalanceBentoEvent) {
        when (event) {
            is BalanceBentoEvent.OnLoad -> {
                val progress = peerManagerSource.getCurrentBlockHeight() / latestLTCBlockHeight.toFloat()
                _state.update {
                    it.copy(
                        lastTimeStamp = "",
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
                        lastTimeStamp = "",
                        syncProgress = event.syncProgress,
                        brainwalletIsSyncing = event.syncProgress <= 0.999f
                    )
                }
            }
        }
    }

    fun debugTriggerStatusUpdate() {
        if (!BuildConfig.DEBUG) return
        onStatusUpdate()
    }

    fun debugTriggerTxAdded() {
        if (!BuildConfig.DEBUG) return
        onTxAdded()
    }

    fun debugTriggerBalanceChanged(balance: Long = 1_000_000L) {
        if (!BuildConfig.DEBUG) return
        onBalanceChanged(balance)
    }
}
