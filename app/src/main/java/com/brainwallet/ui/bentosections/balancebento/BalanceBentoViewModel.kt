package com.brainwallet.ui.bentosections.balancebento

import android.R.attr.progress
import android.app.Application
import androidx.lifecycle.viewModelScope
import com.brainwallet.BuildConfig
import com.brainwallet.data.repository.ConnectivityRepository
import com.brainwallet.data.repository.LtcRepository
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.data.repository.TxRepository
import com.brainwallet.data.source.PeerManagerSource
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.sqlite.TransactionDataSource
import com.brainwallet.tools.util.BRExchange.ONE_LITECOIN_OF_LITOSHIS
import com.brainwallet.ui.BrainwalletViewModel
import com.brainwallet.wallet.BRPeerManager
import com.brainwallet.wallet.BRWalletManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val ltcRepository: LtcRepository,
    private val settingRepository: SettingRepository,
    private val peerManagerSource: PeerManagerSource,
    private val connectivityRepository: ConnectivityRepository,
) : BrainwalletViewModel<BalanceBentoEvent>(),
    BRWalletManager.OnBalanceChanged,
    BRPeerManager.OnTxStatusUpdate,
    TransactionDataSource.OnTxAddedListener {
    private val _state = MutableStateFlow(BalanceBentoState())
    val state: StateFlow<BalanceBentoState> = _state.asStateFlow()
    val formatter = java.text.SimpleDateFormat(
        "MMM dd, yyyy hh:mm a",
        java.util.Locale.getDefault()
    )

    val latestLTCBlockHeight = BRSharedPrefs.getLiveLtcStats(app).currentBlockHeight

    init {
        viewModelScope.launch {
            // Runs immediately on launch
            val progress = peerManagerSource.getCurrentBlockHeight() / latestLTCBlockHeight.toFloat()
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
            ltcRepository.ltcStats.collect { ltcStats ->
                _state.update {
                    Timber.d("timber: ltcStats: $ltcStats")

                    it.copy(
                        ltcStats = ltcStats
                    )
                }
            }
        }
    }

    fun onResume(
        isWalletCreated: () -> Boolean = { BRWalletManager.getInstance().isCreated() },
        ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    ) {
        viewModelScope.launch(ioDispatcher) {
            var attempts = 0

            while (!isWalletCreated() && attempts < 20) {
                delay(250)
                attempts++
            }
            if (isWalletCreated()) {
                addObservers()
                val balance = BRSharedPrefs.getCachedBalance(app)
                val currentSettings = settingRepository.currentSettings.value

                _state.update {
                    it.copy(
                        ltcBalance = balance,
                        selectedCurrency = currentSettings.currency,
                        fiatCode = currentSettings.currency.code,
                        symbol = currentSettings.currency.symbol,
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
        TransactionDataSource.getInstance(app).removeListener(this)
    }
    private fun addObservers() {
        BRWalletManager.getInstance().addBalanceChangedListener(this)
        BRPeerManager.getInstance().addStatusUpdateListener(this)
        TransactionDataSource.getInstance(app).addTxAddedListener(this)
    }
    private fun removeObservers() {
        BRWalletManager.getInstance().removeListener(this)
        BRPeerManager.getInstance().removeListener(this)
        TransactionDataSource.getInstance(app).removeListener(this)
    }

    override fun onBalanceChanged(balance: Long) {
        _state.update {
            val currentSettings = settingRepository.currentSettings.value
            it.copy(
                ltcBalance = balance,
                selectedCurrency = currentSettings.currency,
                fiatCode = currentSettings.currency.code,
                symbol = currentSettings.currency.symbol
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            txRepository.refresh()
        }
    }

    override fun onStatusUpdate() {
        viewModelScope.launch(Dispatchers.IO) {
            val progress = peerManagerSource.getCurrentBlockHeight() / latestLTCBlockHeight.toFloat()
            _state.update {
                it.copy(
                    currentBlockHeight = peerManagerSource.getCurrentBlockHeight()
                )
            }
            onEvent(BalanceBentoEvent.OnUpdatedSyncProgress(progress))
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
                val currentSettings = settingRepository.currentSettings.value
                _state.update {
                    it.copy(
                        lastTimeStamp = "",
                        selectedCurrency = currentSettings.currency,
                        fiatCode = currentSettings.currency.code,
                        symbol = currentSettings.currency.symbol,
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
