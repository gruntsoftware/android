package com.brainwallet.ui.screens.inputwords

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

class InputWordsViewModel : BrainwalletViewModel<InputWordsEvent>() {

    private val _state = MutableStateFlow(InputWordsState())
    val state: StateFlow<InputWordsState> = _state.asStateFlow()

    init {
        //TODO: revisit later, please move to repository, for now just reuse the existing
        Bip39Reader.bip39List(BrainwalletApp.breadContext, Language.ENGLISH.code).also { bip39Words ->
            _state.update { it.copy(bip39Words = bip39Words) }
        }
    }

    override fun onEvent(event: InputWordsEvent) {
        when (event) {
            is InputWordsEvent.OnSeedWordItemChange -> _state.update {
                it.copy(
                    seedWords = it.seedWords.toMutableMap().apply {
                        put(event.index, Bip39Reader.cleanWord(event.text.lowercase()))
                    },
                    suggestionsSeedWords = it.bip39Words.filter {
                        it.startsWith(event.text) && event.text.isNotEmpty()
                    }
                )
            }

            InputWordsEvent.OnClearSeedWords -> _state.update {
                InputWordsState(bip39Words = it.bip39Words)
            }

            is InputWordsEvent.OnLoad -> _state.update { it.copy(source = event.source) }
            is InputWordsEvent.OnRestoreClick -> {
                val currentState = state.value
                val paperKey = currentState.seedWords.asPaperKey()

                val cleanPhrase = SmartValidator.cleanPaperKey(event.context, paperKey)

                if (SmartValidator.isPaperKeyValid(event.context, cleanPhrase)
                        .not() && SmartValidator.isPaperKeyCorrect(cleanPhrase, event.context).not()
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

                if (currentState.isFrom(Route.InputWords.Source.RESET_PIN)) {
                    viewModelScope.launch {
                        EventBus.emit(EventBus.Event.Message(LEGACY_EFFECT_RESET_PIN))
                    }
                    return
                }

                if (currentState.isFrom(Route.InputWords.Source.SETTING_WIPE)) {
                    viewModelScope.launch {
                        EventBus.emit(EventBus.Event.Message(LEGACY_DIALOG_WIPE_ALERT))
                    }
                    return
                }

                BRWalletManager.getInstance().run {
                    wipeWalletButKeystore(event.context)
                    wipeKeyStore(event.context)
                    PostAuth.getInstance().setPhraseForKeyStore(cleanPhrase)
                    BRSharedPrefs.putAllowSpend(event.context, false)
                }

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
