package com.brainwallet.ui.screens.home

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
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
import kotlinx.coroutines.flow.updateAndGet
import kotlinx.coroutines.launch

class SettingsViewModel : BrainwalletViewModel<SettingsEvent>() {
    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private var settingRepository: SettingRepository =
        BrainwalletApp.module!!.settingRepository

    private val appSetting = settingRepository.settings
        .distinctUntilChanged()
        .onEach { setting ->
            _state.update {
                it.copy(
                    darkMode = setting.isDarkMode,
                    selectedLanguage = Language.find(setting.languageCode),
                    selectedCurrency = setting.currency
                )
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            AppSetting()
        )

    override fun onEvent(event: SettingsEvent) {
        when (event) {
            SettingsEvent.OnToggleDarkMode -> viewModelScope.launch {
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

            SettingsEvent.OnToggleLock -> viewModelScope.launch {
                _state.update {
                    val toggled = it.isLocked.not()

                    settingRepository.save(
                        appSetting.value.copy(
                            isLocked = toggled
                        )
                    )

                    it.copy(isLocked = toggled)
                }
            }

            SettingsEvent.OnFiatButtonClick -> _state.update {
                it.copy(fiatSelectorBottomSheetVisible = true)
            }

            SettingsEvent.OnLanguageSelectorButtonClick -> _state.update {
                it.copy(languageSelectorBottomSheetVisible = true)
            }

            SettingsEvent.OnFiatSelectorDismiss -> _state.update {
                it.copy(
                    fiatSelectorBottomSheetVisible = false
                )
            }

            SettingsEvent.OnLanguageSelectorDismiss -> _state.update {
                it.copy(
                    languageSelectorBottomSheetVisible = false
                )
            }

            is SettingsEvent.OnLanguageChange -> _state.updateAndGet {
                it.copy(
                    selectedLanguage = event.language,
                    languageSelectorBottomSheetVisible = false
                )
            }.let {
                viewModelScope.launch {
                    settingRepository.save(appSetting.value.copy(languageCode = event.language.code))
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(
                            event.language.code
                        )
                    )

                }
            }
            is SettingsEvent.OnFiatChange -> _state.updateAndGet {
                it.copy(selectedCurrency = event.currency)
            }.let {
                viewModelScope.launch {
                    settingRepository.save(
                        appSetting.value.copy(
                            currency = event.currency
                        )
                    )
                }
            }
            is SettingsEvent.OnUserDidStartSync -> viewModelScope.launch {
                _state.update {
                    ////DEV : Call the sync function
                    val toggled = it.userDidStartSync.not()
                    it.copy(userDidStartSync = toggled)
                }
            }

        }
    }
}