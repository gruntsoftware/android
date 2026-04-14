package com.brainwallet.ui.bentosections.transactionbento

import androidx.lifecycle.viewModelScope
import com.brainwallet.data.repository.LtcRepository
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.ui.BrainwalletViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import timber.log.Timber

@KoinViewModel
class TransactionBentoViewModel(
    private val settingRepository: SettingRepository,
    private val ltcRepository: LtcRepository,
) : BrainwalletViewModel<TransactionBentoEvent>() {

    private val _state = MutableStateFlow(TransactionBentoState())
    val state: StateFlow<TransactionBentoState> = _state.asStateFlow()
    val formatter = java.text.SimpleDateFormat(
        "MMM dd, yyyy h:mm:ss a",
        java.util.Locale.getDefault()
    )

    init {
        viewModelScope.launch {
            ltcRepository.ltcStats.collect { ltcStats ->
                Timber.i("ltcStats: $ltcStats")
                _state.update {
                    it.copy(
                        ltcStats = ltcStats
                    )
                }
            }
        }

        viewModelScope.launch {
            settingRepository.currentSettings.collect { currentSettings ->
                _state.update {
                    it.copy(
                        selectedCurrency = currentSettings.currency,
                        darkMode = currentSettings.isDarkMode
                    )
                }
            }
        }
    }

    override fun onEvent(event: TransactionBentoEvent) {
        when (event) {
            is TransactionBentoEvent.OnLoad -> {
                Timber.i("TransactionBentoEvent.OnLoad")
            }
            is TransactionBentoEvent.ToggleTransactionViews -> {
                Timber.i("TransactionBentoEvent.ToggleTransactionViews")
            }
            is TransactionBentoEvent.ToggleTransactionFilter -> {
                Timber.i("TransactionBentoEvent.ToggleTransactionFilter")
            }
        }
    }
}
