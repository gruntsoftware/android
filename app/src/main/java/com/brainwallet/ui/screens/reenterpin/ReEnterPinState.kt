package com.brainwallet.ui.screens.reenterpin

data class ReEnterPinState(
    val currentPin: String = "",
    val originalPin: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPinComplete: Boolean = false,
    val showValidationError: Boolean = false
)
