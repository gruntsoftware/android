package com.brainwallet.ui.screens.main
import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.viewModelScope
import com.brainwallet.R
import com.brainwallet.constants.BWConstants
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.GlobalCurrency
import com.brainwallet.data.model.GlobalCurrency.entries
import com.brainwallet.data.repository.ConnectivityRepository
import com.brainwallet.data.repository.LtcRepository
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.data.repository.TxRepository
import com.brainwallet.tools.manager.AnalyticsManager
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.sqlite.TransactionDataSource
import com.brainwallet.tools.util.BRExchange.ONE_LITECOIN_OF_LITOSHIS
import com.brainwallet.ui.BrainwalletViewModel
import com.brainwallet.ui.bentosections.balancebento.BalanceBentoViewModel
import com.brainwallet.ui.bentosections.transactionbento.TransactionFilterState
import com.brainwallet.util.CurrencyDataGetter
import com.brainwallet.util.VersionCodeProvider
import com.brainwallet.wallet.BRPeerManager
import com.brainwallet.wallet.BRWalletManager
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import timber.log.Timber
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(FlowPreview::class)
@KoinViewModel
class MainViewModel(
    private val app: Application,
    private val settingRepository: SettingRepository,
    private val ltcRepository: LtcRepository,
    private val txRepository: TxRepository,
    private val connectivityRepository: ConnectivityRepository,
    private val balanceBentoViewModel: BalanceBentoViewModel,
    versionCodeProvider: VersionCodeProvider,
) : BrainwalletViewModel<MainScreenEvent>(),
    BRWalletManager.OnBalanceChanged,
    BRPeerManager.OnTxStatusUpdate,
    TransactionDataSource.OnTxAddedListener {

    private val _state =
        MutableStateFlow(
            MainScreenState(
                versionLabel = versionCodeProvider
                    .getFormatted()
            )
        )
    val state: StateFlow<MainScreenState> = _state.asStateFlow()
    val appSetting: StateFlow<AppSetting> = settingRepository.currentSettings

    init {

        // ──────── Collecting Reachability Updates ────────
        viewModelScope.launch {
            connectivityRepository.isConnected.collect { isInternetReachable ->
                _state.update { it.copy(isInternetReachable = isInternetReachable) }
            }
        }

        viewModelScope.launch {
            settingRepository.currentSettings.collect { currentSettings ->
                _state.update {
                    it.copy(
                        selectedCurrency = currentSettings.currency,
                    )
                }
            }
        }

        viewModelScope.launch {
            ltcRepository.rates
                .combine(appSetting) { currencies, setting ->
                    currencies.firstOrNull {
                        it.code == setting.currency.code && it.symbol == setting.currency.symbol
                    }
                }
                .filterNotNull()
                .collect { selectedCurrency ->
                    _state.update {
                        it.copy(
                            fiatSymbol = selectedCurrency.symbol,
                            fiatiSOCode = selectedCurrency.code,
                            fiatRate = BigDecimal(selectedCurrency.rate.toDouble()),
                            selectedCurrency = CurrencyEntity(
                                code = selectedCurrency.code,
                                name = selectedCurrency.name,
                                rate = selectedCurrency.rate,
                                symbol = selectedCurrency.symbol,
                            ),
                        )
                    }
                }
        }

        viewModelScope.launch {
            txRepository.transactionItems.collect { items ->
                _state.update {
                    it.copy(
                        transactionItems = items,
                        allTransactionItems = items
                    )
                }
            }
        }

        viewModelScope.launch {
            ltcRepository.ltcStats.collect { ltcStats ->
                _state.update {
                    it.copy(
                        ltcStats = ltcStats
                    )
                }
            }
        }

        viewModelScope.launch {
            state.map { it.fiatAmount }
                .debounce(1000)
                .distinctUntilChanged()
                .filter {
                    val baseCurrency = state.value.moonpayCurrencyLimit.data.baseCurrency
                    val baseCurrencyMin = BigDecimal(baseCurrency.min.toDouble())
                    val baseCurrencyMax = BigDecimal(baseCurrency.max.toDouble())

                    if (baseCurrencyMin == BigDecimal(0) &&
                        baseCurrencyMax == BigDecimal(0)
                    ) {
                        return@filter false
                    }

                    it in baseCurrencyMin..baseCurrencyMax
                }
                .collect {
                    onEvent(MainScreenEvent.OnFiatAmountChangeFromMPLimits(it))
                }
        }

        viewModelScope.launch {
            balanceBentoViewModel.state
                .map { it.brainwalletIsSyncing }
                .distinctUntilChanged()
                .collect { brainwalletIsSyncing ->
                    _state.update { it.copy(brainwalletIsSyncing = brainwalletIsSyncing) }
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
                _state.update {
                    it.copy(
                        ltcBalance = BigDecimal(balance),
                        litoshiBalance = BigDecimal(balance)
                            .divide(
                                BigDecimal(ONE_LITECOIN_OF_LITOSHIS),
                                8,
                                BWConstants.ROUNDING_MODE
                            )
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
            it.copy(
                ltcBalance = BigDecimal(balance),
                litoshiBalance = BigDecimal(balance)
                    .divide(
                        BigDecimal(ONE_LITECOIN_OF_LITOSHIS),
                        8,
                        BWConstants.ROUNDING_MODE
                    ),
            )
        }
        viewModelScope.launch(Dispatchers.IO) {
            txRepository.refresh()
        }
    }

    override fun onStatusPeerManagerUpdate() {
        Timber.d("timber: MainViewModel subscribed onStatusUpdate: : BRPeerManager")
    }

    override fun onTxAdded() {
        viewModelScope.launch(Dispatchers.IO) {
            txRepository.refresh()
        }
    }

    override fun onEvent(event: MainScreenEvent) {
        when (event) {
            is MainScreenEvent.OnLoad -> viewModelScope.launch {
                delay(500)

                _state.update { it.copy(address = BRSharedPrefs.getReceiveAddress(event.context)) }
                try {
                    onLoading(true)
                    txRepository.refresh()
                    val currentSettings = settingRepository.currentSettings.value
                    var formattedCurrency: String? = null
                    val currency = currentSettings.currency
                    val currencyDataGetter = CurrencyDataGetter(event.context)
                    if (currency != null) {
                        val roundedPriceAmount: BigDecimal =
                            BigDecimal(currency.rate.toDouble()).multiply(BigDecimal(100))
                                .divide(BigDecimal(100), 2, BWConstants.ROUNDING_MODE)
                        formattedCurrency =
                            currencyDataGetter.getFormattedCurrencyString(
                                currency.code,
                                roundedPriceAmount
                            )
                    } else {
                        Timber.w("The currency related to %s is NULL", currency.code)
                    }

                    fun from(code: String): GlobalCurrency? {
                        return entries.find { it.code == code }
                    }

                    _state.getAndUpdate {
                        val limitResult = ltcRepository.fetchLimits(
                            baseCurrencyCode = appSetting.value.currency.code
                        )

                        it.copy(
                            moonpayCurrencyLimit = limitResult,
                            fiatAmount = BigDecimal(limitResult.data.baseCurrency.min.toDouble()),
                            selectedCurrency = currentSettings.currency,
                            fiatiSOCode = currentSettings.currency.code,
                            fiatSymbol = currentSettings.currency.symbol,
                            formattedCurrency = formattedCurrency ?: ""
                        )
                    }
                } catch (e: Exception) {
                    handleError(e)
                } finally {
                    onLoading(false)
                }
            }

            is MainScreenEvent.OnFiatAmountChangeFromMPLimits -> viewModelScope.launch {
                // do validation based on the MP Limits
                val (_, min, max) = state.value.moonpayCurrencyLimit.data.baseCurrency
                val errorStringId = when {
                    event.fiatAmount < BigDecimal(min.toDouble()) -> R.string.buy_litecoin_fiat_amount_validation_min
                    event.fiatAmount > BigDecimal(max.toDouble()) -> R.string.buy_litecoin_fiat_amount_validation_max
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
                            ltcAmount = BigDecimal(result.data.quoteCurrencyAmount.toDouble()),
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

            is MainScreenEvent.OnToggleTransactionsDetail -> viewModelScope.launch {
                _state.update { it.copy(showTransactionDetail = !it.showTransactionDetail) }
            }

            is MainScreenEvent.OnToggleTransactionsFilter -> viewModelScope.launch {
                _state.update {
                    val nextFilter = TransactionFilterState.entries[
                        (it.filterState.ordinal + 1) % TransactionFilterState.entries.size
                    ]

                    val filteredTransactions = when (nextFilter) {
                        TransactionFilterState.ALL -> it.allTransactionItems
                        TransactionFilterState.RECEIVED -> it.allTransactionItems.filter {
                                txItem ->
                            (txItem.received - txItem.sent > 0)
                        }.toImmutableList()
                        TransactionFilterState.SENT -> it.allTransactionItems.filter {
                                txItem ->
                            (txItem.received - txItem.sent < 0)
                        }.toImmutableList()
                    }
                    it.copy(
                        filterState = nextFilter,
                        transactionItems = filteredTransactions
                    )
                }
                AnalyticsManager.logCustomAdHocEvent("did_toggle_txn_filter")
            }
            is MainScreenEvent.OnExportTransactions -> {
                // TODO: Implement
            }
            is MainScreenEvent.OnCopyTransactions -> {
                val currentTransaction = event.transactionItem

                val ltcAddressString = currentTransaction?.to?.firstOrNull() ?: ""
                val formatter = SimpleDateFormat(
                    "MMM dd, yyyy hh:mm a",
                    Locale.getDefault()
                )
                val dateTimestamp = formatter.format(Date(currentTransaction?.timeStamp?.times(1000L) ?: 0L))
                val wasReceived = currentTransaction?.getSent() == 0L
                val amountReceived =
                    BigDecimal(currentTransaction?.received ?: 0L).divide(
                        BigDecimal(ONE_LITECOIN_OF_LITOSHIS),
                        8,
                        BWConstants.ROUNDING_MODE
                    )

                val outAmounts: LongArray? = currentTransaction?.getOutAmounts()
                var opsAmount = Long.Companion.MAX_VALUE
                if (outAmounts?.size == 3) {
                    for (i in outAmounts.indices) {
                        val value = outAmounts[i]

                        if (value < opsAmount && value != 0L) {
                            opsAmount = value
                            Timber.d("timber: outAmounts size %d opsAmount value: %d", outAmounts.size, value)
                        }
                    }
                } else {
                    opsAmount = 0L
                }

                val txWasReceived = currentTransaction?.getReceived() ?: 0L
                val txWasSent = currentTransaction?.getSent() ?: 0L

                val sentLitoshisAmount: Long = when {
                    wasReceived -> txWasReceived
                    else -> txWasSent - txWasReceived - opsAmount
                }
                val isLTCPreferred = BRSharedPrefs.getLTCViewingPreference(app)
                val iso = if (isLTCPreferred) "LTC" else BRSharedPrefs.getIsoSymbol(app)
                val txAmount = BigDecimal(currentTransaction.getReceived() - currentTransaction.getSent())
                    .abs().divide(BigDecimal(ONE_LITECOIN_OF_LITOSHIS))
                val wasSentVsReceived = currentTransaction.received - currentTransaction.sent < 0

                val amountString = if (wasSentVsReceived) {
                    String.format(
                        "-Ł $txAmount"
                    )
                } else {
                    String.format("+Ł $txAmount")
                }

                val combinedFees = BigDecimal(currentTransaction?.fee ?: 0L) + BigDecimal(opsAmount)
                val feesLitoshis = combinedFees.divide(BigDecimal(ONE_LITECOIN_OF_LITOSHIS))
                val feesTotal = String.format("-Ł $feesLitoshis")
                val txHxString = currentTransaction?.txHashHexReversed ?: ""
                val txIDBrowserURL: String = run {
                    "${BWConstants.BLOCKCHAIR_EXPLORER_BASE_URL}$txHxString"
                }

                val transactionDetailsString = """
                    LTC Address: $ltcAddressString  Timestamp: $dateTimestamp
                    Amount: $amountString  Fees: $feesTotal
                    Transaction Hash: $txHxString
                    Blockchain browser URL: $txIDBrowserURL
                """.trimIndent()

                try {
                    val clipboard = app.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData
                        .newPlainText("Transaction Details: ", transactionDetailsString)
                    clipboard.setPrimaryClip(clip)
                    AnalyticsManager.logCustomEventWithParams("txn_details_copied", null)
                } catch (e: java.lang.Exception) {
                    Timber.e(e)
                }
            }
        }
    }
}
