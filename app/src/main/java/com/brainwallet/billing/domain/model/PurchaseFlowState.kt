package com.brainwallet.billing.domain.model

data class PurchaseFlowState(
    val isLoading: Boolean = false,
    val purchaseResult: PurchaseResult? = null,
    val errorMessage: String? = null
)
