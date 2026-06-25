package com.brainwallet.ui.screens.gamehub
import androidx.work.Data

data class GameHubState(
    val languageISO: String = "en_US",
    val lastGameResult: Data? = null
)
