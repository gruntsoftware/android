package com.brainwallet.ui.screens.welcome

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.model.Language
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.ui.BrainwalletViewModel
import com.brainwallet.wallet.BRWalletManager
import com.brainwallet.worker.SyncBlockWorker
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

//TODO: revisit this later
class WelcomeViewModel(
    private val settingRepository: SettingRepository
) : BrainwalletViewModel<WelcomeEvent>() {

    private val _state = MutableStateFlow(WelcomeState())
    val state: StateFlow<WelcomeState> = _state.asStateFlow()

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

            WelcomeEvent.OnFiatButtonClick -> _state.update {
                it.copy(fiatSelectorBottomSheetVisible = true)
            }

            WelcomeEvent.OnLanguageSelectorButtonClick -> _state.update {
                it.copy(languageSelectorBottomSheetVisible = true)
            }

            WelcomeEvent.OnFiatSelectorDismiss -> _state.update {
                it.copy(
                    fiatSelectorBottomSheetVisible = false
                )
            }

            WelcomeEvent.OnLanguageSelectorDismiss -> _state.update {
                it.copy(
                    languageSelectorBottomSheetVisible = false
                )
            }

            is WelcomeEvent.OnLanguageChange -> _state.updateAndGet {
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

            is WelcomeEvent.OnFiatChange -> _state.updateAndGet {
                it.copy(selectedCurrency = event.currency, fiatSelectorBottomSheetVisible = false)
            }.let {
                viewModelScope.launch {
                    settingRepository.save(
                        appSetting.value.copy(
                            currency = event.currency
                        )
                    )
                }
            }

            is WelcomeEvent.OnLoad -> {
                val walletNotExists = BRWalletManager.getInstance().noWallet(event.context)
                if (walletNotExists) {
                    BRWalletManager.getInstance().generateRandomSeed(event.context)
                }
                WorkManager.getInstance(event.context).enqueue(SyncBlockWorker.request)
            }
        }
    }

}