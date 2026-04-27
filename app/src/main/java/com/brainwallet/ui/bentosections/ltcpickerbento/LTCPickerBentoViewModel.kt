package com.brainwallet.ui.bentosections.ltcpickerbento

import androidx.lifecycle.viewModelScope
import com.brainwallet.constants.BWConstants
import com.brainwallet.data.repository.LtcRepository
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.tools.sqlite.CurrencyDataSource
import com.brainwallet.ui.BrainwalletViewModel
import com.brainwallet.util.CurrencyDataGetter
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.delay
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
            while (true) {
                try { fetchAndUpdateRates() } catch (e: Exception) {
                    Timber.e(e, "fetchRates failed")
                    FirebaseCrashlytics.getInstance().recordException(e)
                }
                delay(5000L)
            }
        }
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
    }
    private suspend fun fetchAndUpdateRates(
        currencyCode: String = settingRepository.currentSettings.value.currency.code
    ) {
        val rates = ltcRepository.fetchRates()
        val selectedFiat = rates.find { it.code == currencyCode }
        val iso = currencyDataGetter.getIsoSymbol()

        var formattedCurrency: String? = null
        val currency = currencyDataGetter.getCurrencyByIso(iso)
        if (currency != null) {
            val roundedPriceAmount: BigDecimal =
                BigDecimal(currency.rate.toDouble()).multiply(BigDecimal(100))
                    .divide(BigDecimal(100), 2, BWConstants.ROUNDING_MODE)
            formattedCurrency =
                currencyDataGetter.getFormattedCurrencyString(
                    iso,
                    roundedPriceAmount
                )
        } else {
            Timber.w("The currency related to %s is NULL", iso)
        }
        _state.update {
            it.copy(
                selectedCurrency = selectedFiat ?: return@update it,
                formattedFiat = formattedCurrency ?: "",
                formattedTimeStamp = formatter.format(java.util.Date()),
                ltcStats = ltcRepository.ltcStats.value
            )
        }
    }
    override fun onEvent(event: LTCPickerBentoEvent) {
        when (event) {
            is LTCPickerBentoEvent.OnGlobalCurrencyChange -> {
                val newCurrency = currencyDataSource.getCurrencyByIso(event.globalCurrency.code)
                if (newCurrency != null) {
                    viewModelScope.launch {
                        settingRepository.save(settingRepository.currentSettings.value.copy(currency = newCurrency))
                        fetchAndUpdateRates(newCurrency.code)
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
