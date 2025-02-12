package com.brainwallet.ui.screens.welcome

sealed class WelcomeEvent {
    object OnBackClick : WelcomeEvent()

    data class OnSeedWordItemChange(
        val index: Int,
        val text: String
    ) : WelcomeEvent()

    object OnClearSeedWords : WelcomeEvent()

    data class OnRestoreClick(val paperkey: String) : WelcomeEvent()
}