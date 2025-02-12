package com.brainwallet.ui.screens.yourseedwords

sealed class YourSeedWordsEvent {
    object OnBackClick : YourSeedWordsEvent()
    object OnSavedItClick : YourSeedWordsEvent()
}