package com.brainwallet.billing.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.billingclient.api.Purchase

@Entity(tableName = "purchase_transactions")
data class PurchaseTransactionEntity(
    @PrimaryKey
    @ColumnInfo(name = "purchase_token")
    val purchaseToken: String,
    
    @ColumnInfo(name = "product_id")
    val productId: String,
    
    @ColumnInfo(name = "product_type")
    val productType: String, // INAPP or SUBS
    
    @ColumnInfo(name = "purchase_state")
    val purchaseState: Int, // Purchase.PurchaseState
    
    @ColumnInfo(name = "is_acknowledged")
    val isAcknowledged: Boolean,
    
    @ColumnInfo(name = "purchase_time")
    val purchaseTime: Long,
    
    @ColumnInfo(name = "order_id")
    val orderId: String?,
    
    @ColumnInfo(name = "package_name")
    val packageName: String,
    
    @ColumnInfo(name = "signature")
    val signature: String,
    
    @ColumnInfo(name = "original_json")
    val originalJson: String,
    
    @ColumnInfo(name = "acknowledgment_attempts")
    val acknowledgmentAttempts: Int = 0,
    
    @ColumnInfo(name = "last_acknowledgment_attempt")
    val lastAcknowledgmentAttempt: Long? = null,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromPurchase(purchase: Purchase, productType: String): PurchaseTransactionEntity {
            return PurchaseTransactionEntity(
                purchaseToken = purchase.purchaseToken,
                productId = purchase.products.firstOrNull() ?: "",
                productType = productType,
                purchaseState = purchase.purchaseState,
                isAcknowledged = purchase.isAcknowledged,
                purchaseTime = purchase.purchaseTime,
                orderId = purchase.orderId,
                packageName = purchase.packageName,
                signature = purchase.signature,
                originalJson = purchase.originalJson
            )
        }
    }
    
    fun needsAcknowledgment(): Boolean {
        return purchaseState == Purchase.PurchaseState.PURCHASED && !isAcknowledged
    }
    
    fun shouldRetryAcknowledgment(): Boolean {
        if (!needsAcknowledgment()) return false
        
        // Don't retry if we've already tried too many times
        if (acknowledgmentAttempts >= 5) return false
        
        // If no previous attempt, we should try
        if (lastAcknowledgmentAttempt == null) return true
        
        // Exponential backoff: wait longer between retries
        val timeSinceLastAttempt = System.currentTimeMillis() - lastAcknowledgmentAttempt
        val minWaitTime = when (acknowledgmentAttempts) {
            0 -> 0L
            1 -> 30_000L // 30 seconds
            2 -> 300_000L // 5 minutes
            3 -> 1_800_000L // 30 minutes
            else -> 3_600_000L // 1 hour
        }
        
        return timeSinceLastAttempt >= minWaitTime
    }
}