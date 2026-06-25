package com.brainwallet.ui.screens.gamehub

import android.content.Context
import androidx.work.Data

sealed class GameHubEvent {
    data class OnLoad(val context: Context) : GameHubEvent()
    data class OnGameExited(val jsonPayload: String?, val endData: Data?) : GameHubEvent()
}
