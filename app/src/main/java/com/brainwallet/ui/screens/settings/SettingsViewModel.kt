package com.brainwallet.ui.screens.settings

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.viewModelScope
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.model.Language
import com.brainwallet.data.model.toFeeOptions
import com.brainwallet.data.repository.LtcRepository
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.domain.LanguageSwitcherUseCase
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.manager.FeeManager
import com.brainwallet.tools.security.BRKeyStore
import com.brainwallet.tools.util.TrustedNode
import com.brainwallet.ui.BrainwalletViewModel
import com.brainwallet.wallet.BRPeerManager
import com.brainwallet.util.EventBus
import com.brainwallet.util.VersionCodeProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class SettingsViewModel(
    private val settingRepository: SettingRepository,
    private val languageSwitcherUseCase: LanguageSwitcherUseCase,
    private val ltcRepository: LtcRepository,
    versionCodeProvider: VersionCodeProvider,
    private val app: Application,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : BrainwalletViewModel<SettingsEvent>() {

    private val _state = MutableStateFlow(SettingsState(formattedVersion = versionCodeProvider.getFormatted()))
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

    init {

        viewModelScope.launch {
            settingRepository.settings.collect { appSetting ->
                _state.update {
                    it.copy(
                        darkMode = appSetting.isDarkMode,
                        selectedLanguage = Language.find(appSetting.languageCode),
                        selectedCurrency = appSetting.currency,
                    )
                }
            }
        }
        viewModelScope.launch(ioDispatcher) {
            while (true) {
                /**
                 * need update fee options every 4s, since we are fetching every 4s
                 * pls check at
                 * - [CurrencyUpdateWorker]
                 * - [LtcRepository.fetchRates]
                 * - [LtcRepository.fetchFeePerKb]
                 */

                _state.update {
                    it.copy(currentFeeOptions = FeeManager.getInstance().currentFeeOptions.toFeeOptions())
                }
                delay(4000)
            }
        }
        loadTrustedNode()
    }

    /**
     * Reads the persisted trusted-node address off the main thread (BRKeyStore does blocking
     * disk/Keystore IO) and publishes it to [SettingsState.trustedNodeAddress].
     */
    private fun loadTrustedNode() {
        viewModelScope.launch(ioDispatcher) {
            val address = runCatching { BRKeyStore.getTrustedNodeIPAddress(app, 0) }.getOrNull()
            _state.update { it.copy(trustedNodeAddress = address) }
        }
    }
    fun hasUserSetEmojis(): Boolean {
        return BRSharedPrefs.wereEmojisChosen(app)
    }

    override fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.OnLoad -> viewModelScope.launch {
                _state.update {
                    it.copy(
                        shareAnalyticsDataEnabled = event.shareAnalyticsDataEnabled,
                        lastSyncMetadata = event.lastSyncMetadata,
                        selectedFeeType = settingRepository.getSelectedFeeType()
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
                EventBus.emit(
                    EventBus.Event.Message(
                        LEGACY_EFFECT_ON_TOGGLE_DARK_MODE,
                        address = null
                    )
                )
            }

            SettingsEvent.OnToggleLock -> viewModelScope.launch {
                EventBus.emit(EventBus.Event.Message(LEGACY_EFFECT_ON_LOCK, null))
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
                    languageSwitcherUseCase.switchLanguage(event.language)
                    AppCompatDelegate.setApplicationLocales(
                        LocaleListCompat.forLanguageTags(
                            event.language.code
                        )
                    )
                }
            }

            is SettingsEvent.OnFiatChange -> viewModelScope.launch {
                settingRepository.save(
                    AppSetting(
                        isDarkMode = _state.value.darkMode,
                        languageCode = _state.value.selectedLanguage.code,
                        currency = event.currency
                    )
                )
            }

            is SettingsEvent.OnBlockchainSyncClick -> viewModelScope.launch {
                EventBus.emit(EventBus.Event.Message(LEGACY_EFFECT_ON_SYNC, address = null))
            }

            SettingsEvent.OnTrustedNodePurchased -> {
                // Purchase unlocked the feature; the address-entry sheet opens next in the UI.
                // Refresh so the row label reflects any address a previous purchase already set.
                loadTrustedNode()
            }

            is SettingsEvent.OnTrustedNodeAddressSubmitted -> viewModelScope.launch(ioDispatcher) {
                val address = event.address.trim()
                if (!TrustedNode.isValid(address)) return@launch

                val stored = runCatching {
                    BRKeyStore.putTrustedNodeIPAddress(address, app, 0)
                }.getOrDefault(false)
                if (!stored) return@launch

                // Keystore is the single source of truth; BRPeerManager.updateFixedPeer()
                // reads it back and repoints the SPV connection at the new node.
                runCatching { BRPeerManager.getInstance().updateFixedPeer(app) }
                loadTrustedNode()
            }

            SettingsEvent.OnSecuritySeedPhraseClick -> viewModelScope.launch {
                EventBus.emit(
                    EventBus.Event.Message(
                        LEGACY_EFFECT_ON_SEED_PHRASE,
                        address = null
                    )
                )
            }

            SettingsEvent.OnSecurityBrainwalletPhraseClick -> viewModelScope.launch {
                EventBus.emit(
                    EventBus.Event.Message(
                        LEGACY_EFFECT_ON_BRAINWALLET_PHRASE,
                        address = null
                    )
                )
            }

            SettingsEvent.OnSecurityUpdatePinClick -> viewModelScope.launch {
                EventBus.emit(
                    EventBus.Event.Message(
                        LEGACY_EFFECT_ON_SEC_UPDATE_PIN,
                        address = null
                    )
                )
            }

            SettingsEvent.OnSecurityShareAnalyticsDataClick -> viewModelScope.launch {
                _state.update { it.copy(shareAnalyticsDataEnabled = it.shareAnalyticsDataEnabled.not()) }

                EventBus.emit(
                    EventBus.Event.Message(
                        LEGACY_EFFECT_ON_SHARE_ANALYTICS_DATA_TOGGLE,
                        address = null
                    )
                )
            }

            is SettingsEvent.OnFeeTypeChange -> _state.update {
                settingRepository.putSelectedFeeType(event.feeType)
                it.copy(selectedFeeType = event.feeType)
            }
        }
    }

    companion object {
        const val LEGACY_EFFECT_ON_LOCK = "onLockInvoked"
        const val LEGACY_EFFECT_ON_TOGGLE_DARK_MODE = "onToggleDarkMode"
        const val LEGACY_EFFECT_ON_SYNC = "onSyncInvoked"
        const val LEGACY_EFFECT_ON_SEC_UPDATE_PIN = "onSecUpdatePin"
        const val LEGACY_EFFECT_ON_SEED_PHRASE = "onSeedPhrase"

        const val LEGACY_EFFECT_ON_BRAINWALLET_PHRASE = "onBrainwalletPhrase"
        const val LEGACY_EFFECT_ON_SHARE_ANALYTICS_DATA_TOGGLE = "onShareAnalyticsDataToggle"
    }
}
