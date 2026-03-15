package com.brainwallet.ui.bentosections.balancebento

import androidx.lifecycle.viewModelScope
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.ui.BrainwalletViewModel
import com.brainwallet.worker.CurrencyUpdateWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class BalanceBentoViewModel(
    private val settingRepository: SettingRepository,
    private val currencyUpdateWorker: CurrencyUpdateWorker
) : BrainwalletViewModel<BalanceBentoEvent>() {

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
                    formattedTimeStamp = formatter.format(java.util.Date())
                )
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            AppSetting()
        )

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
