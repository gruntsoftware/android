package com.brainwallet.billing.domain.model

data class PurchaseResult(
    val isSuccessful: Boolean,
    val errorMessage: String? = null,
    val userCancelled: Boolean = false
)
