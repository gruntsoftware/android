package com.brainwallet.ui.screens.yourseedwords

sealed class YourSeedWordsEvent {
    data class OnSavedItClick(val seedWords: List<String>) : YourSeedWordsEvent()
}