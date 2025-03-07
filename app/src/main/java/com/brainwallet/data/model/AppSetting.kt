package com.brainwallet.data.model

data class AppSetting(
    val isLocked: Boolean = false,
    val isDarkMode: Boolean = true,
    val languageCode: String = Language.ENGLISH.code,
    val currency: CurrencyEntity = CurrencyEntity(
        "USD",
        "US Dollar",
        -1f,
        "$"
    )
)