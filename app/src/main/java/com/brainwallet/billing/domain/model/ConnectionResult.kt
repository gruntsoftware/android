package com.brainwallet.billing.domain.model

data class ConnectionResult(
    val isConnected: Boolean,
    val errorMessage: String? = null
)
