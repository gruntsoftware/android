package com.brainwallet.ui.bentosections.ltcpickerbento

import androidx.lifecycle.viewModelScope
import com.brainwallet.constants.BWConstants
import com.brainwallet.data.repository.LtcRepository
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.tools.sqlite.CurrencyDataSource
import com.brainwallet.ui.BrainwalletViewModel
import com.brainwallet.util.CurrencyDataGetter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import timber.log.Timber
import java.math.BigDecimal

@KoinViewModel
class LTCPickerBentoViewModel(
    private val settingRepository: SettingRepository,
    private val currencyDataSource: CurrencyDataSource,
    private val currencyDataGetter: CurrencyDataGetter,
    private val ltcRepository: LtcRepository
) : BrainwalletViewModel<LTCPickerBentoEvent>() {

    private val _state = MutableStateFlow(LTCPickerBentoState())
    val state: StateFlow<LTCPickerBentoState> = _state.asStateFlow()
    val formatter = java.text.SimpleDateFormat(
        "MMM dd, h:mm:ss a",
        java.util.Locale.getDefault()
    )

    init {
        viewModelScope.launch {
            settingRepository.settings.collect { setting ->
                _state.update {
                    it.copy(
                        darkMode = setting.isDarkMode,
                        selectedCurrency = setting.currency
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

        viewModelScope.launch {
            ltcRepository.rates.collect { rates ->
                runCatching {
                    Timber.d("timber|| rates in fetching $rates")
                    val currencyCode = _state.value.selectedCurrency.code
                    val selectedFiat = rates.find { it.code == currencyCode }
                    val iso = currencyDataGetter.getIsoSymbol()
                    val currency = currencyDataGetter.getCurrencyByIso(iso)

                    val formattedCurrency = currency?.let {
                        val rounded = BigDecimal(it.rate.toDouble())
                            .multiply(BigDecimal(100))
                            .divide(BigDecimal(100), 2, BWConstants.ROUNDING_MODE)
                        currencyDataGetter.getFormattedCurrencyString(iso, rounded)
                    }

                    if (selectedFiat != null) {
                        _state.update {
                            it.copy(
                                selectedCurrency = selectedFiat,
                                formattedFiat = formattedCurrency ?: "",
                                formattedTimeStamp = formatter.format(java.util.Date()),
                                ltcStats = ltcRepository.ltcStats.value
                            )
                        }
                    }
                }.onFailure { Timber.e(it, "timber|| rates collector crashed") }
            }
        }
    }

    override fun onEvent(event: LTCPickerBentoEvent) {
        when (event) {
            is LTCPickerBentoEvent.OnGlobalCurrencyChange -> {
                val newCurrency = currencyDataSource.getCurrencyByIso(event.globalCurrency.code)
                if (newCurrency != null) {
                    viewModelScope.launch {
                        settingRepository.save(settingRepository.currentSettings.value.copy(currency = newCurrency))
                    }
                } else {
                    Timber.w("Currency not found for code: ${event.globalCurrency.code}")
                }
            }
            is LTCPickerBentoEvent.OnLoad -> {
                _state.update { currentState ->
                    currentState.copy(
                        formattedTimeStamp = formatter.format(java.util.Date())
                    )
                }
            }
            is LTCPickerBentoEvent.OnLiveCurrencyUpdate -> {
                _state.update { currentState ->
                    currentState.copy(
                        formattedTimeStamp = formatter.format(java.util.Date())
                            .replace("AM", "am")
                            .replace("PM", "pm")
                    )
                }
            }
        }
    }
}
