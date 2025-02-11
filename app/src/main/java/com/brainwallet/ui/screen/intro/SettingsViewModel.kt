package com.brainwallet.ui.screen.intro

import androidx.lifecycle.ViewModel
import com.brainwallet.presenter.activities.util.ThemePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ThemeViewModel(private val themePreferences: ThemePreferences) : ViewModel() {
    private val _isDarkMode = MutableStateFlow(themePreferences.isDarkMode())
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    fun toggleTheme() {
        val newTheme = !_isDarkMode.value
        themePreferences.saveTheme(newTheme)
        _isDarkMode.value = newTheme
    }
}