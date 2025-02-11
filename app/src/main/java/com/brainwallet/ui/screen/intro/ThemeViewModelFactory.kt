package com.brainwallet.ui.screen.intro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.brainwallet.presenter.activities.util.ThemePreferences

class ThemeViewModelFactory(private val themePreferences: ThemePreferences) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ThemeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ThemeViewModel(themePreferences) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}