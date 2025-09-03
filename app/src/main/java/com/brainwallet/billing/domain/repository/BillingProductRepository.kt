package com.brainwallet.billing.domain.repository

import com.brainwallet.billing.domain.model.ProductQueryResult

interface BillingProductRepository {
    suspend fun queryAvailableProducts(productIds: List<String>, productType: String): ProductQueryResult
}