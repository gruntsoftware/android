package com.brainwallet.ui.screens.yourseedproveit

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.brainwallet.navigation.Route
import com.brainwallet.navigation.UiEffect
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.ui.BrainwalletViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class YourSeedProveItViewModel(
    private val application: Application
) : BrainwalletViewModel<YourSeedProveItEvent>() {

    private val _state = MutableStateFlow(YourSeedProveItState())
    val state: StateFlow<YourSeedProveItState> = _state.asStateFlow()

    override fun onEvent(event: YourSeedProveItEvent) {
        when (event) {
            is YourSeedProveItEvent.OnLoad -> _state.update {
                val correctSeedWords = event.seedWords.mapIndexed { index, word ->
                    index to SeedWordItem(expected = word)
                }.toMap()

                it.copy(
                    correctSeedWords = correctSeedWords,
                    shuffledSeedWords = correctSeedWords.map { it.key to it.value.expected }.shuffled(),
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

            YourSeedProveItEvent.OnGameAndSync -> sendUiEffect(
                UiEffect.Navigate(
                    destinationRoute = Route.Main,
                    forcePopBackStack = true
                )
            )

            YourSeedProveItEvent.OnCompletedPaperKey -> viewModelScope.launch {
                BRSharedPrefs.putPhraseWroteDown(application, true)
                sendUiEffect(
                    UiEffect.Navigate(
                        destinationRoute = Route.TopUp(),
                        forcePopBackStack = true
                    )
                )
            }
        }
    }
}
