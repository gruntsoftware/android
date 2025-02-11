package com.brainwallet.presenter.activities.util

import android.content.Context
import android.content.SharedPreferences

class ThemePreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("theme_prefs", Context.MODE_PRIVATE)

    fun isDarkMode(): Boolean = prefs.getBoolean("is_dark_mode", false)

    fun saveTheme(isDark: Boolean) {
        prefs.edit().putBoolean("is_dark_mode", isDark).apply()
    }
}