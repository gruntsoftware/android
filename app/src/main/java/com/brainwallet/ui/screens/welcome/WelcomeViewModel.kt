package com.brainwallet.ui.screens.welcome

import androidx.lifecycle.viewModelScope
import com.brainwallet.BrainwalletApp
import com.brainwallet.data.model.AppSetting
import com.brainwallet.ui.BrainwalletViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class WelcomeViewModel : BrainwalletViewModel<WelcomeEvent>() {

    private val _state = MutableStateFlow(WelcomeState())
    val state: StateFlow<WelcomeState> = _state.asStateFlow()

    override fun onEvent(event: WelcomeEvent) {
        when (event) {
            WelcomeEvent.OnToggleDarkMode -> viewModelScope.launch {
                _state.update {
                    val toggled = it.darkMode.not()

                    //simulate dark theme toggle to repo
                    val settingRepository = BrainwalletApp.module.settingRepository.save(
                        AppSetting(isDarkMode = toggled)
                    )

                    it.copy(darkMode = toggled)
                }
            }
        }
    }

}