package com.brainwallet.billing.data.repository

import com.android.billingclient.api.Purchase
import com.brainwallet.billing.data.source.local.dao.PurchaseTransactionDao
import com.brainwallet.billing.data.source.local.entity.PurchaseTransactionEntity
import com.brainwallet.billing.domain.repository.BillingLocalRepository

class BillingLocalRepositoryImpl(
    private val purchaseTransactionDao: PurchaseTransactionDao
) : BillingLocalRepository, PurchaseTransactionDao by purchaseTransactionDao {

    override suspend fun savePurchaseTransaction(purchase: Purchase, productType: String) {
        val entity = PurchaseTransactionEntity.fromPurchase(purchase, productType)
        purchaseTransactionDao.insertPurchaseTransaction(entity)
    }

    override suspend fun savePurchaseTransactions(purchases: List<Purchase>, productType: String) {
        val entities = purchases.map { purchase ->
            PurchaseTransactionEntity.fromPurchase(purchase, productType)
        }
        purchaseTransactionDao.insertPurchaseTransactions(entities)
    }

    override suspend fun markPurchaseAcknowledged(purchaseToken: String) {
        purchaseTransactionDao.markAsAcknowledged(purchaseToken)
    }

    override suspend fun incrementAcknowledgmentAttempt(purchaseToken: String) {
        purchaseTransactionDao.incrementAcknowledgmentAttempt(purchaseToken)
    }
}
