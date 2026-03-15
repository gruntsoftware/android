package com.brainwallet.ui.bentosections.ltcpickerbento

import androidx.lifecycle.viewModelScope
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.tools.sqlite.CurrencyDataSource
import com.brainwallet.ui.BrainwalletViewModel
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
import timber.log.Timber

@KoinViewModel
class LTCPickerBentoViewModel(
    private val settingRepository: SettingRepository,
    private val currencyDataSource: CurrencyDataSource
) : BrainwalletViewModel<LTCPickerBentoEvent>() {

    private val _state = MutableStateFlow(LTCPickerBentoState())
    val state: StateFlow<LTCPickerBentoState> = _state.asStateFlow()
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
                    formattedTimeStamp = formatter.format(java.util.Date())
                )
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            AppSetting()
        )

    override fun onEvent(event: LTCPickerBentoEvent) {
        when (event) {
            is LTCPickerBentoEvent.OnGlobalCurrencyChange -> {
                val newCurrency = currencyDataSource.getCurrencyByIso(event.globalCurrency.code)
                if (newCurrency != null) {
                    val dateString = formatter.format(java.util.Date())

                    _state.update { currentState ->
                        currentState.copy(
                            selectedCurrency = newCurrency
                        )
                    }

                    viewModelScope.launch {
                        settingRepository.save(
                            appSetting.value.copy(currency = newCurrency)
                        )
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
