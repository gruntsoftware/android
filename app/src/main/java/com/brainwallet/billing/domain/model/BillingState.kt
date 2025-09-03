package com.brainwallet.billing.domain.model

data class BillingState(
    val isConnected: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
