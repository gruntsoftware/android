package com.brainwallet.ui.screens.home

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewModelScope
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.model.Language
import com.brainwallet.data.model.toFeeOptions
import com.brainwallet.data.repository.LtcRepository
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.ui.BrainwalletViewModel
import com.brainwallet.util.EventBus
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


class SettingsViewModel(
    private val settingRepository: SettingRepository,
    private val ltcRepository: LtcRepository
) : BrainwalletViewModel<SettingsEvent>() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    val appSetting = settingRepository.settings
        .distinctUntilChanged()
        .onEach { setting ->
            _state.update {
                it.copy(
                    darkMode = setting.isDarkMode,
                    selectedLanguage = Language.find(setting.languageCode),
                    selectedCurrency = setting.currency,
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
            is SettingsEvent.OnLoad -> viewModelScope.launch {
                _state.update {
                    it.copy(
                        shareAnalyticsDataEnabled = event.shareAnalyticsDataEnabled,
                        lastSyncMetadata = event.lastSyncMetadata,
                        currentFeeOptions = ltcRepository.fetchFeePerKb().toFeeOptions()
                    )
                }
            }

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
                EventBus.emit(EventBus.Event.Message(LEGACY_EFFECT_ON_TOGGLE_DARK_MODE))
            }

            SettingsEvent.OnToggleLock -> viewModelScope.launch {
                EventBus.emit(EventBus.Event.Message(LEGACY_EFFECT_ON_LOCK))
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

            is SettingsEvent.OnBlockchainSyncClick -> viewModelScope.launch {
                EventBus.emit(EventBus.Event.Message(LEGACY_EFFECT_ON_SYNC))
            }

            SettingsEvent.OnSecuritySeedPhraseClick -> viewModelScope.launch {
                EventBus.emit(EventBus.Event.Message(LEGACY_EFFECT_ON_SEED_PHRASE))
            }

            SettingsEvent.OnSecurityUpdatePinClick -> viewModelScope.launch {
                EventBus.emit(EventBus.Event.Message(LEGACY_EFFECT_ON_SEC_UPDATE_PIN))
            }

            SettingsEvent.OnSecurityShareAnalyticsDataClick -> viewModelScope.launch {
                _state.update { it.copy(shareAnalyticsDataEnabled = it.shareAnalyticsDataEnabled.not()) }

                EventBus.emit(EventBus.Event.Message(LEGACY_EFFECT_ON_SHARE_ANALYTICS_DATA_TOGGLE))
            }
        }
    }

    companion object {
        const val LEGACY_EFFECT_ON_LOCK = "onLockInvoked"
        const val LEGACY_EFFECT_ON_TOGGLE_DARK_MODE = "onToggleDarkMode"
        const val LEGACY_EFFECT_ON_SYNC = "onSyncInvoked"
        const val LEGACY_EFFECT_ON_SEC_UPDATE_PIN = "onSecUpdatePin"
        const val LEGACY_EFFECT_ON_SEED_PHRASE = "onSeedPhrase"
        const val LEGACY_EFFECT_ON_SHARE_ANALYTICS_DATA_TOGGLE = "onShareAnalyticsDataToggle"
    }
}