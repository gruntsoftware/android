package com.brainwallet.ui.screens.gamehub

import android.content.Context

sealed class GameHubEvent {
    data class OnLoad(val context: Context) : GameHubEvent()
    data class OnGameExited(val jsonPayload: String, val byteArray: ByteArray) : GameHubEvent()
    data object OnGameFinished : GameHubEvent()

    /** The game's exit payload parsed, so it counts toward unlocking emojis 4 - 6. */
    data object OnGameScoreRecorded : GameHubEvent()
}
