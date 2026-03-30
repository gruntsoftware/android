package com.brainwallet.ui.screens.restore

import androidx.lifecycle.viewModelScope
import com.brainwallet.BrainwalletApp
import com.brainwallet.data.model.Language
import com.brainwallet.navigation.Route
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.security.PostAuth
import com.brainwallet.tools.security.SmartValidator
import com.brainwallet.tools.util.Bip39Reader
import com.brainwallet.ui.BrainwalletViewModel
import com.brainwallet.util.EventBus
import com.brainwallet.wallet.BRWalletManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class RestoreViewModel : BrainwalletViewModel<RestoreEvent>() {

    private val _state = MutableStateFlow(RestoreState())
    val state: StateFlow<RestoreState> = _state.asStateFlow()

    init {
        // TODO: revisit later, please move to repository, for now just reuse the existing
        Bip39Reader.bip39List(BrainwalletApp.breadContext, Language.ENGLISH.code)
            .also { bip39Words ->
                _state.update { it.copy(bip39Words = bip39Words) }
            }
    }

    override fun onEvent(event: RestoreEvent) {
        when (event) {
            is RestoreEvent.OnSeedWordItemChange -> _state.update {
                it.copy(
                    seedWords = it.seedWords.toMutableMap().apply {
                        put(event.index, Bip39Reader.cleanWord(event.text.lowercase()))
                    },
                    suggestionsSeedWords = it.bip39Words.filter {
                        it.startsWith(event.text) && event.text.isNotEmpty()
                    }
                )
            }

            RestoreEvent.OnClearSeedWords -> _state.update {
                RestoreState(bip39Words = it.bip39Words)
            }

            is RestoreEvent.OnLoad -> _state.update { it.copy(source = event.source) }
            is RestoreEvent.OnRestoreClick -> {
                val currentState = state.value
                val paperKey = currentState.seedWords.asPaperKey()
                val cleanPhrase = SmartValidator.cleanPaperKey(event.context, paperKey)

                if (currentState.isFromWelcome() &&
                    SmartValidator.isPaperKeyValid(event.context, cleanPhrase).not()
                ) {
                    viewModelScope.launch {
                        EventBus.emit(
                            EventBus.Event.Message(
                                LEGACY_DIALOG_INVALID
                            )
                        )
                    }
                    return
                }

                if (currentState.isFromWelcome().not() &&
                    SmartValidator.isPaperKeyCorrect(cleanPhrase, event.context).not()
                ) {
                    viewModelScope.launch {
                        EventBus.emit(
                            EventBus.Event.Message(
                                LEGACY_DIALOG_INVALID
                            )
                        )
                    }
                    return
                }

                if (currentState.isFrom(Route.Restore.Source.RESET_PIN)) {
                    viewModelScope.launch {
                        EventBus.emit(EventBus.Event.Message(LEGACY_EFFECT_RESET_PIN))
                    }
                    return
                }

                if (currentState.isFrom(Route.Restore.Source.SETTING_WIPE)) {
                    viewModelScope.launch {
                        EventBus.emit(EventBus.Event.Message(LEGACY_DIALOG_WIPE_ALERT))
                    }
                    return
                }

                BRWalletManager.getInstance().wipeAll(event.context)
                BRSharedPrefs.putAllowSpend(event.context, false)
                BRSharedPrefs.putStartHeight(event.context, 0)
                PostAuth.getInstance().setPhraseForKeyStore(cleanPhrase)

                viewModelScope.launch {
                    EventBus.emit(EventBus.Event.Message(EFFECT_LEGACY_RECOVER_WALLET_AUTH))
                }
            }
        }
    }

    companion object {
        const val LEGACY_DIALOG_INVALID = "dialog_invalid"
        const val LEGACY_DIALOG_WIPE_ALERT = "dialog_wipe_alert"
        const val EFFECT_LEGACY_RECOVER_WALLET_AUTH = "onRecoverWalletAuth"
        const val LEGACY_EFFECT_RESET_PIN = "onResetPin"
    }
}
