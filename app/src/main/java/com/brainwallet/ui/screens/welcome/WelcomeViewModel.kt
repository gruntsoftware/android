package com.brainwallet.ui.screens.welcome

import androidx.lifecycle.viewModelScope
import com.brainwallet.BrainwalletApp
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.model.Language
import com.brainwallet.data.repository.SettingRepository
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

//TODO: revisit this later
class WelcomeViewModel : BrainwalletViewModel<WelcomeEvent>() {

    private val _state = MutableStateFlow(WelcomeState())
    val state: StateFlow<WelcomeState> = _state.asStateFlow()

    private var settingRepository: SettingRepository =
        BrainwalletApp.module!!.settingRepository //for now just inject here

    private val appSetting = settingRepository.settings
        .distinctUntilChanged()
        .onEach { setting ->
            _state.update {
                it.copy(
                    darkMode = setting.isDarkMode,
                    selectedLanguage = Language.find(setting.languageCode),
                )
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            AppSetting()
        )

    override fun onEvent(event: WelcomeEvent) {
        when (event) {
            WelcomeEvent.OnToggleDarkMode -> viewModelScope.launch {
                _state.update {
                    val toggled = it.darkMode.not()

                    settingRepository.save(
                        appSetting.value.copy(
                            isDarkMode = toggled
                        )
                    )

                    it.copy(darkMode = toggled)
                }
            }
        }
    }

}