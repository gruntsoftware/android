package com.brainwallet.ui.bentosections.transactionbento

import androidx.lifecycle.viewModelScope
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.ui.BrainwalletViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.android.annotation.KoinViewModel
import timber.log.Timber
import com.brainwallet.data.model.AppSetting
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
@KoinViewModel
class TransactionBentoViewModel(
    private val settingRepository: SettingRepository,
) : BrainwalletViewModel<TransactionBentoEvent>() {

    private val _state = MutableStateFlow(TransactionBentoState())
    val state: StateFlow<TransactionBentoState> = _state.asStateFlow()

    private val appSetting = settingRepository.settings
        .distinctUntilChanged()
        .onEach { setting ->
            _state.update {
                it.copy(darkMode = setting.isDarkMode)
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppSetting())

    val formatter = java.text.SimpleDateFormat(
        "MMM dd, yyyy h:mm:ss a",
        java.util.Locale.getDefault()
    )

    init {
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
