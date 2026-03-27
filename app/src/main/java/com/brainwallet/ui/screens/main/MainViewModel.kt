package com.brainwallet.ui.screens.main
import android.app.Application
import androidx.lifecycle.viewModelScope
import com.brainwallet.R
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.repository.LtcRepository
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.data.repository.TxRepository
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.sqlite.TransactionDataSource
import com.brainwallet.ui.BrainwalletViewModel
import com.brainwallet.util.VersionCodeProvider
import com.brainwallet.wallet.BRPeerManager
import com.brainwallet.wallet.BRWalletManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import timber.log.Timber

@OptIn(FlowPreview::class)
@KoinViewModel
class MainViewModel(
    private val app: Application,
    private val settingRepository: SettingRepository,
    private val ltcRepository: LtcRepository,
    private val txRepository: TxRepository,
    versionCodeProvider: VersionCodeProvider,
) : BrainwalletViewModel<MainScreenEvent>(),
    BRWalletManager.OnBalanceChanged,
    BRPeerManager.OnTxStatusUpdate,
    BRSharedPrefs.OnIsoChangedListener,
    TransactionDataSource.OnTxAddedListener {

    private val _state =
        MutableStateFlow(
            MainScreenState(
                versionLabel = versionCodeProvider
                    .getFormatted()
            )
        )
    val state: StateFlow<MainScreenState> = _state.asStateFlow()

    val appSetting = settingRepository.settings
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            AppSetting()
        )

    val versionLabel = versionCodeProvider.getFormatted()

    init {
        // Currency rate → fiat fields in _state
        viewModelScope.launch {
            ltcRepository.rates
                .combine(appSetting) { currencies, setting ->
                    currencies.firstOrNull { it.code == setting.currency.code }
                }
                .filterNotNull()
                .collect { selectedCurrency ->
                    val msg = String.format("selectedCurrency — Name: %s", selectedCurrency.name)
                    Timber.d("ISO: %s", msg)
                    FirebaseCrashlytics.getInstance().log(msg)
                    _state.update {
                        it.copy(
                            fiatSymbol = selectedCurrency.symbol,
                            fiatIso = selectedCurrency.code,
                            fiatRate = selectedCurrency.rate,
                        )
                    }
                }
        }

        // Transactions → _state directly, no intermediate StateFlow copy
        viewModelScope.launch {
            txRepository.transactionItems.collect { items ->
                Timber.d("timber: transactions updated: ${items.size}")
                _state.update { it.copy(transactionItems = items) }
            }
        }

        // Fiat amount debounce validation (unchanged)
        viewModelScope.launch {
            state.map { it.fiatAmount }
                .debounce(1000)
                .distinctUntilChanged()
                .filter {
                    val baseCurrency = state.value.moonpayCurrencyLimit.data.baseCurrency
                    if (baseCurrency.min == 0f && baseCurrency.max == 0f) return@filter false
                    it in baseCurrency.min..baseCurrency.max
                }
                .collect {
                    onEvent(MainScreenEvent.OnFiatAmountChange(it))
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
                txRepository.refresh()
            } else {
                Timber.d("MainViewModel: wallet not ready after waiting")
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

    // /Callbacks fron BRWalletManager, TransactionDataSource, BRSharedPrefs
    override fun onBalanceChanged(balance: Long) {
        Timber.d("timber: MainViewModel subscribed onBalanceChanged $balance")
        viewModelScope.launch(Dispatchers.IO) {
            txRepository.refresh()

            Timber.d("MainViewModel: TxRepository refreshing ")
        }
    }

    override fun onStatusUpdate() {
        Timber.d("timber: MainViewModel subscribed onStatusUpdate: : BRPeerManager")
    }

    override fun onIsoChanged(iso: String?) {
        val isoString = iso ?: ""
        Timber.d("timber: MainViewModel subscribed onIsoChanged $isoString")
    }

    override fun onTxAdded() {
        Timber.d("timber: MainViewModel subscribed onTxAdded: TransactionDataSource")
    }

    override fun onEvent(event: MainScreenEvent) {
        when (event) {
            is MainScreenEvent.OnLoad -> viewModelScope.launch {
                delay(500)

                _state.update { it.copy(address = BRSharedPrefs.getReceiveAddress(event.context)) }
                try {
                    onLoading(true)
                    txRepository.refresh()
                    _state.getAndUpdate {
                        val limitResult = ltcRepository.fetchLimits(
                            baseCurrencyCode = appSetting.value.currency.code
                        )

                        it.copy(
                            moonpayCurrencyLimit = limitResult,
                            fiatAmount = limitResult.data.baseCurrency.min,
                        )
                    }
                } catch (e: Exception) {
                    handleError(e)
                } finally {
                    onLoading(false)
                }
            }

            is MainScreenEvent.OnFiatAmountChange -> viewModelScope.launch {
                // do validation
                val (_, min, max) = state.value.moonpayCurrencyLimit.data.baseCurrency
                val errorStringId = when {
                    event.fiatAmount < min -> R.string.buy_litecoin_fiat_amount_validation_min
                    event.fiatAmount > max -> R.string.buy_litecoin_fiat_amount_validation_max
                    else -> null
                }
                _state.update {
                    it.copy(
                        errorFiatAmountStringId = errorStringId,
                        fiatAmount = event.fiatAmount
                    )
                }

                if (event.needFetch.not()) {
                    return@launch
                }

                try {
                    onLoading(true)

                    _state.update {
                        val result = ltcRepository.fetchBuyQuote(
                            mapOf(
                                "currencyCode" to "ltc",
                                "baseCurrencyCode" to appSetting.value.currency.code,
                                "baseCurrencyAmount" to event.fiatAmount.toString(),
                            )
                        )

                        it.copy(
                            ltcAmount = result.data.quoteCurrencyAmount,
                        )
                    }
                } catch (e: Exception) {
                    handleError(e)
                } finally {
                    onLoading(false)
                }
            }
            is MainScreenEvent.OnToggleDarkMode -> viewModelScope.launch {
                val currentSettings = appSetting.value
                settingRepository.save(
                    currentSettings.copy(isDarkMode = !currentSettings.isDarkMode)
                )
            }
        }
    }
}
