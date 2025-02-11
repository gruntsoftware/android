package com.brainwallet.ui.screen.inputwords

sealed class InputWordsEvent {
    object OnBackClick : InputWordsEvent()

    data class OnSeedWordItemChange(
        val index: Int,
        val text: String
    ) : InputWordsEvent()

    object OnClearSeedWords : InputWordsEvent()

    data class OnRestoreClick(val paperkey: String) : InputWordsEvent()
}