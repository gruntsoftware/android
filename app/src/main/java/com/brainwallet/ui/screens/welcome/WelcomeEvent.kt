package com.brainwallet.ui.screens.welcome

sealed class WelcomeEvent {
    object OnToggleTheme : WelcomeEvent()
    object OnReadyClick: WelcomeEvent()
    object OnRestoreClick: WelcomeEvent()
}