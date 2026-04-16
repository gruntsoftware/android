package com.brainwallet.ui.screens.gamehub

import android.content.Context

sealed class GameHubEvent {
    data class OnLoad(val context: Context) : GameHubEvent()
}
