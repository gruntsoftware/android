package com.brainwallet.billing.domain.model

data class AcknowledgmentResult(
    val isSuccessful: Boolean,
    val errorMessage: String? = null,
    val shouldRetryLater: Boolean = false
)
