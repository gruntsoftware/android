package com.brainwallet.ui.screen.inputwords

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InputWordsViewModel : ViewModel() {

    private val _state = MutableStateFlow(InputWordsState())
    val state: StateFlow<InputWordsState> = _state.asStateFlow()

    fun onEvent(event: InputWordsEvent) {
        //todo
        when (event) {
            is InputWordsEvent.OnSeedWordItemChange -> _state.update {
                it.copy(
                    seedWords = it.seedWords.toMutableMap().apply {
                        put(event.index, event.text)
                    }
                )
            }

            InputWordsEvent.OnClearSeedWords -> _state.update {
                InputWordsState()
            }

            else -> Unit
        }
    }

}