package com.brainwallet.ui.screens.gamehub

import android.content.Context

sealed class GameHubEvent {
    data class OnLoad(val context: Context) : GameHubEvent()
    data class OnGameExited(val jsonPayload: String, val byteArray: ByteArray) : GameHubEvent()
    data object OnGameFinished : GameHubEvent()

    /**
     * The game's exit payload parsed, so it's saved to the game history. [gameTimestamp] is the
     * game's own timestamp in ms, or 0 when it didn't send one.
     */
    data class OnGameScoreRecorded(val totalScore: Int, val gameTimestamp: Long) : GameHubEvent()
}
