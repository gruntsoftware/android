package com.brainwallet.data.model

data class AppSetting(
    val isDarkMode: Boolean = true,
    val languageCode: String = Language.ENGLISH.code
)