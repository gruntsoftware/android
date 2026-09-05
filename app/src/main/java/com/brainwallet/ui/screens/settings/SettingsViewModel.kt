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
import com.grunt.brainwallet.iap.domain.usecase.CheckTrustedLTCNodeEntitlementUseCase
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
import org.koin.core.context.GlobalContext

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
        loadTrustedNodeEntitlement()
    }

    /**
     * Reads the persisted trusted-node host, port and sync preference off the main thread
     * (BRKeyStore does blocking disk/Keystore IO) and publishes them to
     * [SettingsState.trustedNodeAddress] / [SettingsState.trustedNodePort] /
     * [SettingsState.userPrefersTrustedNode]. Host and port are stored as independent
     * BRKeyStore values - the port is just as required as the host - and only combined back
     * into a "host:port" form where that's actually needed, via [TrustedNode.withPort]. The
     * sync preference is its own stored flag and defaults to false (default peer discovery)
     * when nothing has been persisted yet, so the toggle in [LitecoinBlockchainDetail]
     * always renders the last value the user chose.
     */
    private fun loadTrustedNode() {
        viewModelScope.launch(ioDispatcher) {
            val address = runCatching { BRKeyStore.getTrustedNodeIPAddress(app, 0) }.getOrNull()
            val port = runCatching { BRKeyStore.getTrustedNodePort(app, 0) }.getOrDefault(0)
            val prefersTrustedNode =
                runCatching { BRKeyStore.getTrustedNodeSyncPreference(app, 0) }.getOrDefault(false)
            _state.update {
                it.copy(
                    trustedNodeAddress = address,
                    trustedNodePort = port.takeIf { storedPort -> storedPort > 0 }?.toString(),
                    userPrefersTrustedNode = prefersTrustedNode
                )
            }
        }
    }

    /**
     * Best-effort check of whether this account owns the trusted-LTC-node unlock, so the
     * settings row can open the IP editor directly instead of the paywall. Any failure
     * (IAP module not loaded, RevenueCat unreachable) leaves [SettingsState.trustedNodeEntitled]
     * false and the paywall is shown.
     */
    private fun loadTrustedNodeEntitlement() {
        val checkEntitlement = GlobalContext.getOrNull()
            ?.getOrNull<CheckTrustedLTCNodeEntitlementUseCase>() ?: return
        viewModelScope.launch(ioDispatcher) {
            val entitled = runCatching { checkEntitlement(Unit) }.getOrDefault(false)
            _state.update { it.copy(trustedNodeEntitled = entitled) }
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

            is SettingsEvent.OnTrustedNodeToggle -> {
                // Reflect the switch immediately (synchronous state update), then persist the
                // preference off the main thread so it survives process death and is what
                // loadTrustedNode() reads back the next time the row is shown.
                _state.update { it.copy(userPrefersTrustedNode = event.userPrefersTrustedNode) }
                viewModelScope.launch(ioDispatcher) {
                    runCatching {
                        BRKeyStore.putTrustedNodeSyncPreference(event.userPrefersTrustedNode, app, 0)
                    }
                    // Apply the new mode to the live SPV sync now, not just on next launch.
                    // updateFixedPeer() re-reads the preference just persisted above and does a
                    // stop -> re-resolve fixed peer -> restart in either direction:
                    //  - to "Litecoin mainnet": clears any pinned peer, sync restarts against
                    //    the random mainnet peer array;
                    //  - to trusted-node: re-pins the stored node as the fixed peer and
                    //    restarts the sync against it (or stays on mainnet until an address
                    //    is entered, which triggers updateFixedPeer() again).
                    runCatching { BRPeerManager.getInstance().updateFixedPeer(app) }
                }
            }

            is SettingsEvent.OnTrustedNodePurchased -> {
                // Purchase unlocked the feature; the address-entry sheet opens next in the UI.
                // Refresh so the row reflects the new entitlement and any address a previous
                // purchase already set.
                loadTrustedNode()
                loadTrustedNodeEntitlement()
            }

            is SettingsEvent.OnTrustedNodeAddressSubmitted -> viewModelScope.launch(ioDispatcher) {
                val submitted = event.addressAndPort.trim()
                if (!TrustedNode.isValid(submitted)) return@launch
                val host = TrustedNode.getNodeHost(submitted)
                val port = TrustedNode.getNodePort(submitted)
                    .let { parsed -> if (parsed > 0) parsed else TrustedNode.STANDARD_PORT }

                val addressStored = runCatching {
                    BRKeyStore.putTrustedNodeIPAddress(host, app, 0)
                }.getOrDefault(false)
                val portStored = runCatching {
                    BRKeyStore.putTrustedNodePort(port, app, 0)
                }.getOrDefault(false)
                if (!addressStored || !portStored) return@launch
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
