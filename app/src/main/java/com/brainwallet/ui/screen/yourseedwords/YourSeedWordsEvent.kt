package com.brainwallet.ui.screen.yourseedwords

sealed class YourSeedWordsEvent {
    object OnBackClick : YourSeedWordsEvent()
    object OnSavedItClick : YourSeedWordsEvent()
}