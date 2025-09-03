package com.brainwallet.billing.domain.model

data class ProductQueryResult(
    val products: List<com.android.billingclient.api.ProductDetails>,
    val errorMessage: String? = null
)