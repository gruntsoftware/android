package com.brainwallet.ui.screens.yourseedproveit

import androidx.lifecycle.viewModelScope
import com.brainwallet.ui.BrainwalletViewModel
import com.brainwallet.util.EventBus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class YourSeedProveItViewModel : BrainwalletViewModel<YourSeedProveItEvent>() {

    private val _state = MutableStateFlow(YourSeedProveItState())
    val state: StateFlow<YourSeedProveItState> = _state.asStateFlow()

    override fun onEvent(event: YourSeedProveItEvent) {
        when (event) {
            is YourSeedProveItEvent.OnLoad -> _state.update {
                it.copy(
                    correctSeedWords = event.seedWords.mapIndexed { index, word -> 
                        index to SeedWordItem(expected = word) 
                    }.toMap(),
                    shuffledSeedWords = event.seedWords.shuffled()
                )
            }

            is YourSeedProveItEvent.OnDropSeedWordItem -> _state.update {
                val correctSeedWords = it.correctSeedWords.map { (index, seedWordItem) ->
                    if (index == event.index && seedWordItem.expected == event.expectedWord) {
                        index to seedWordItem.copy(actual = event.actualWord)
                    } else {
                        index to seedWordItem
                    }
                }.toMap()

                it.copy(
                    correctSeedWords = correctSeedWords,
                    orderCorrected = correctSeedWords.all { (_, seedWordItem) -> seedWordItem.expected == seedWordItem.actual }
                )
            }

            YourSeedProveItEvent.OnClear -> _state.update {
                it.copy(
                    correctSeedWords = it.correctSeedWords.mapValues { SeedWordItem(expected = it.value.expected) }
                )
            }

            YourSeedProveItEvent.OnGameAndSync -> viewModelScope.launch {
                EventBus.emit(EventBus.Event.Message(LEGACY_EFFECT_ON_PAPERKEY_PROVED))
            }
        }
    }

    companion object {
        const val LEGACY_EFFECT_ON_PAPERKEY_PROVED = "onPaperKeyProved"
    }
}