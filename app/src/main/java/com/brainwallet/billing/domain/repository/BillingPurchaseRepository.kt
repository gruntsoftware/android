package com.brainwallet.billing.domain.repository

import com.android.billingclient.api.Purchase
import com.brainwallet.billing.domain.model.AcknowledgmentResult

interface BillingPurchaseRepository {
    suspend fun acknowledgePurchaseWithRetry(purchaseToken: String): AcknowledgmentResult
    suspend fun queryUserPurchases(productType: String): Result<List<Purchase>>
}