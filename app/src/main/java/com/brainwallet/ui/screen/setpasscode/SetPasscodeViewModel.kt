package com.brainwallet.ui.screen.setpasscode

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SetPasscodeViewModel : ViewModel() {

    private val _state = MutableStateFlow(SetPasscodeState())
    val state: StateFlow<SetPasscodeState> = _state.asStateFlow()

    fun onEvent(event: SetPasscodeScreenEvent) {
        when (event) {
//            is SetPasscodeScreenEvent.OnLoad -> _state.update {
//                it.copy(
//                    correctSeedWords = event.seedWords.associateWith { "" },
//                    shuffledSeedWords = event.seedWords.shuffled()
//                )
//            }
//
//            is YourSeedProveItEvent.OnDropSeedWordItem -> _state.update {
//                val correctSeedWords = it.correctSeedWords.toMutableMap().apply {
//                    this[event.expectedWord] = event.actualWord
//                }
//
//                it.copy(
//                    correctSeedWords = correctSeedWords,
//                    orderCorrected = correctSeedWords.all { (expectedWord, actualWord) -> expectedWord == actualWord }
//                )
//            }
//
//            YourSeedProveItEvent.OnClear -> _state.update {
//                it.copy(
//                    correctSeedWords = it.correctSeedWords.mapValues { "" }
//                )
//            }

            else -> Unit
        }
    }
}