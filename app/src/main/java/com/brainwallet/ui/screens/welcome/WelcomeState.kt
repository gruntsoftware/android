package com.brainwallet.ui.screens.welcome

enum class UserThemePreference {
    PRIMARY,
    SECONDARY_LIGHT,
    TERTIARY_DARK
}

data class WelcomeState(
    val userThemePreference: UserThemePreference = UserThemePreference.PRIMARY,
)
