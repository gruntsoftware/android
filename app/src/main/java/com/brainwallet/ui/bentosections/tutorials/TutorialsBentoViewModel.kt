package com.brainwallet.ui.bentosections.tutorials

import androidx.lifecycle.viewModelScope
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.ui.BrainwalletViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class TutorialsBentoViewModel(
    private val settingRepository: SettingRepository,
) : BrainwalletViewModel<TutorialBentoEvent>() {

    private val _state = MutableStateFlow(TutorialBentoState())
    val state: StateFlow<TutorialBentoState> = _state.asStateFlow()
    init {
        viewModelScope.launch {
            settingRepository.settings.collect { setting ->
                _state.update {
                    it.copy(
                        darkMode = setting.isDarkMode,
                    )
                }
            }
        }
    }

    override fun onEvent(event: TutorialBentoEvent) {
        when (event) {
            is TutorialBentoEvent.OnLoad -> {
            }
            is TutorialBentoEvent.OnTapGeneralTutorial -> {
            }
            is TutorialBentoEvent.OnTapSendTutorial -> {
            }
        }
    }
}
