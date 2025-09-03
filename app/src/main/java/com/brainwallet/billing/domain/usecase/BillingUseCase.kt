package com.brainwallet.billing.domain.usecase

import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.brainwallet.billing.data.repository.BillingRepository
import com.brainwallet.billing.domain.model.AcknowledgmentResult
import org.koin.core.annotation.Single

@Single
class BillingUseCase(
    private val repository: BillingRepository
) {
    suspend fun validateAndAcknowledgePurchases(): List<AcknowledgmentResult> {
        val results = mutableListOf<AcknowledgmentResult>()
        val inAppPurchases =
            repository.queryUserPurchases(BillingClient.ProductType.INAPP).getOrNull()
                ?: emptyList()
        val subscriptions =
            repository.queryUserPurchases(BillingClient.ProductType.SUBS).getOrNull() ?: emptyList()

        val allPurchases = inAppPurchases + subscriptions

        // Save all purchases to local database first
        if (inAppPurchases.isNotEmpty()) {
            repository.savePurchaseTransactions(inAppPurchases, BillingClient.ProductType.INAPP)
        }
        if (subscriptions.isNotEmpty()) {
            repository.savePurchaseTransactions(subscriptions, BillingClient.ProductType.SUBS)
        }

        allPurchases.forEach { purchase ->
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                val result = repository.acknowledgePurchaseWithRetry(purchase.purchaseToken)
                results.add(result)

                // Update local database based on result
                if (result.isSuccessful) {
                    repository.markPurchaseAcknowledged(purchase.purchaseToken)
                } else {
                    repository.incrementAcknowledgmentAttempt(purchase.purchaseToken)
                }
            }
        }

        return results
    }
}
