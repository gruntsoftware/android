package com.brainwallet.ui.screens.welcome

import androidx.lifecycle.ViewModel
import com.brainwallet.BrainwalletApp
import com.brainwallet.tools.util.Bip39Reader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class WelcomeViewModel : ViewModel() {

    private val _state = MutableStateFlow(WelcomeState())
    val state: StateFlow<WelcomeState> = _state.asStateFlow()

    init {
        //TODO: revisit later, please move to repostiory, for now just reuse the existing
        Bip39Reader.bip39List(BrainwalletApp.getBreadContext(), "en").also { bip39Words ->
            _state.update { it.copy(bip39Words = bip39Words) }
        }
    }

    fun onEvent(event: WelcomeEvent) {
        when (event) {
            is WelcomeEvent.OnSeedWordItemChange -> _state.update {
                it.copy(
                    seedWords = it.seedWords.toMutableMap().apply {
                        put(event.index, Bip39Reader.cleanWord(event.text.lowercase()))
                    },
                    suggestionsSeedWords = it.bip39Words.filter {
                        it.startsWith(event.text) && event.text.isNotEmpty()
                    }
                )
            }

            WelcomeEvent.OnClearSeedWords -> _state.update {
                WelcomeState(bip39Words = it.bip39Words)
            }

            else -> Unit
        }
    }

}