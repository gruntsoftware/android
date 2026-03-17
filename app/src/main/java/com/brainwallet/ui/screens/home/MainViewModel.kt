package com.brainwallet.ui.screens.home
import androidx.lifecycle.viewModelScope
import com.brainwallet.R
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.repository.LtcRepository
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.ui.BrainwalletViewModel
import com.brainwallet.util.VersionCodeProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import timber.log.Timber

@KoinViewModel
class MainViewModel(
    private val settingRepository: SettingRepository,
    private val ltcRepository: LtcRepository,
    versionCodeProvider: VersionCodeProvider,
) : BrainwalletViewModel<MainScreenEvent>() {

    private val _state =
        MutableStateFlow(
            MainScreenState(
                versionLabel = versionCodeProvider
                    .getFormatted()
            )
        )
    val state: StateFlow<MainScreenState> = _state.asStateFlow()
    val currencyRates: StateFlow<List<CurrencyEntity>> = ltcRepository.rates
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val appSetting = settingRepository.settings
        .distinctUntilChanged()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            AppSetting()
        )

    val versionLabel = versionCodeProvider.getFormatted()

    init {
        viewModelScope.launch {
            state.map { it.fiatAmount }
                .debounce(1000)
                .distinctUntilChanged()
                .filter {
                    val (_, min, max) = state.value.moonpayCurrencyLimit.data.baseCurrency
                    it in min..max
                }
                .collect {
                    onEvent(MainScreenEvent.OnFiatAmountChange(it))
                }
        }
        viewModelScope.launch {
            currencyRates.collect { updatedCurrencies ->
                val selectedCurrency = updatedCurrencies.find { it.code == appSetting.value.currency.code }
                Timber.d("selectedCurrency: $selectedCurrency")
            }
        }

        viewModelScope.launch {
            currencyRates.combine(appSetting) { currencies, setting ->
                currencies.find { it.code == setting.currency.code }
            }.collect { selectedCurrency ->
                val msg = String.format("selectedCurrency — Name: %s", selectedCurrency?.name ?: "none")

                _state.update {
                    it.copy(
                        fiatSymbol = selectedCurrency?.symbol.orEmpty(),
                        fiatIso = selectedCurrency?.code.orEmpty(),
                        fiatRate = selectedCurrency?.rate ?: 0f,
                    )
                }
            }
        }
    }

    override fun onEvent(event: MainScreenEvent) {
        when (event) {
            is MainScreenEvent.OnLoad -> viewModelScope.launch {
                delay(500)

                _state.update { it.copy(address = BRSharedPrefs.getReceiveAddress(event.context)) }
                try {
                    onLoading(true)

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
