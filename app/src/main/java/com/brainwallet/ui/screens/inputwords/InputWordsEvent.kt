package com.brainwallet.ui.screens.inputwords

import android.content.Context
import com.brainwallet.navigation.Route

sealed class InputWordsEvent {
    data class OnLoad(
        val source: Route.InputWords.Source? = null
    ) : InputWordsEvent()

    data class OnSeedWordItemChange(
        val index: Int,
        val text: String
    ) : InputWordsEvent()

    object OnClearSeedWords : InputWordsEvent()

    data class OnRestoreClick(
        val context: Context
    ) : InputWordsEvent()
}