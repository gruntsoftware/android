package com.brainwallet.billing.domain.repository

import com.android.billingclient.api.Purchase
import com.brainwallet.billing.data.source.local.dao.PurchaseTransactionDao

interface BillingLocalRepository: PurchaseTransactionDao {
    suspend fun savePurchaseTransaction(purchase: Purchase, productType: String)

    suspend fun savePurchaseTransactions(purchases: List<Purchase>, productType: String)

    suspend fun markPurchaseAcknowledged(purchaseToken: String)

    suspend fun incrementAcknowledgmentAttempt(purchaseToken: String)
}