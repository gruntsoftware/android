package com.brainwallet.ui.screens.gamehub

import com.brainwallet.data.repository.LtcRepository
import com.brainwallet.ui.BrainwalletViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class GameHubViewModel(
    private val ltcRepository: LtcRepository
) : BrainwalletViewModel<GameHubEvent>() {

    private val _state =
        MutableStateFlow(GameHubState())
    val state: StateFlow<GameHubState> = _state.asStateFlow()

    override fun onEvent(event: GameHubEvent) {
        when (event) {
            is GameHubEvent.OnLoad -> {
            }
        }
    }
}
