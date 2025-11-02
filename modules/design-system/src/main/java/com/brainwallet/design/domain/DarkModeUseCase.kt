package com.brainwallet.design.domain

interface DarkModeUseCase {
    fun isDarkMode(): Boolean
    fun toggleDarkMode(isDarkMode: Boolean)
}
