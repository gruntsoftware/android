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
            is GameHubEvent.OnGameExited -> {
                event.jsonPayload?.let { jsonString ->
                    print("jsonString: $jsonString")
                }
                event.endData?.let { endData ->
                    print("endData: $endData")

                    // Then read it back as bytes
//                    val pngBytes: ByteArray? = Gdx.files.external("end_game_screenshot.png")
//                        .readBytes()
                }
            }
        }
    }
}
