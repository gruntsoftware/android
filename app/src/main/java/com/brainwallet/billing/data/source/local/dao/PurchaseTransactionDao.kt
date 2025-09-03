package com.brainwallet.billing.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.brainwallet.billing.data.source.local.entity.PurchaseTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseTransactionDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseTransaction(transaction: PurchaseTransactionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseTransactions(transactions: List<PurchaseTransactionEntity>)
    
    @Update
    suspend fun updatePurchaseTransaction(transaction: PurchaseTransactionEntity)
    
    @Query("SELECT * FROM purchase_transactions WHERE purchase_token = :purchaseToken")
    suspend fun getPurchaseTransaction(purchaseToken: String): PurchaseTransactionEntity?
    
    @Query("SELECT * FROM purchase_transactions WHERE is_acknowledged = 0 AND purchase_state = 1")
    suspend fun getUnacknowledgedPurchases(): List<PurchaseTransactionEntity>
    
    @Query("SELECT * FROM purchase_transactions WHERE is_acknowledged = 0 AND purchase_state = 1")
    fun getUnacknowledgedPurchasesFlow(): Flow<List<PurchaseTransactionEntity>>
    
    @Query("""
        SELECT * FROM purchase_transactions 
        WHERE is_acknowledged = 0 
        AND purchase_state = 1 
        AND (acknowledgment_attempts < 5)
        AND (last_acknowledgment_attempt IS NULL 
             OR (strftime('%s', 'now') * 1000 - last_acknowledgment_attempt) >= 
                CASE acknowledgment_attempts
                    WHEN 0 THEN 0
                    WHEN 1 THEN 30000
                    WHEN 2 THEN 300000
                    WHEN 3 THEN 1800000
                    ELSE 3600000
                END)
        ORDER BY created_at ASC
    """)
    suspend fun getPurchasesReadyForAcknowledgment(): List<PurchaseTransactionEntity>
    
    @Query("UPDATE purchase_transactions SET acknowledgment_attempts = acknowledgment_attempts + 1, last_acknowledgment_attempt = :timestamp, updated_at = :timestamp WHERE purchase_token = :purchaseToken")
    suspend fun incrementAcknowledgmentAttempt(purchaseToken: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("UPDATE purchase_transactions SET is_acknowledged = 1, updated_at = :timestamp WHERE purchase_token = :purchaseToken")
    suspend fun markAsAcknowledged(purchaseToken: String, timestamp: Long = System.currentTimeMillis())
    
    @Query("SELECT * FROM purchase_transactions WHERE product_type = :productType ORDER BY purchase_time DESC")
    suspend fun getPurchasesByType(productType: String): List<PurchaseTransactionEntity>
    
    @Query("SELECT * FROM purchase_transactions WHERE product_id = :productId ORDER BY purchase_time DESC")
    suspend fun getPurchasesByProductId(productId: String): List<PurchaseTransactionEntity>
    
    @Query("SELECT COUNT(*) FROM purchase_transactions WHERE is_acknowledged = 0 AND purchase_state = 1")
    suspend fun getUnacknowledgedPurchaseCount(): Int
    
    @Query("SELECT COUNT(*) FROM purchase_transactions WHERE is_acknowledged = 0 AND purchase_state = 1")
    fun getUnacknowledgedPurchaseCountFlow(): Flow<Int>
    
    @Query("DELETE FROM purchase_transactions WHERE purchase_token = :purchaseToken")
    suspend fun deletePurchaseTransaction(purchaseToken: String)
    
    @Query("DELETE FROM purchase_transactions WHERE created_at < :timestamp")
    suspend fun deleteOldTransactions(timestamp: Long)
    
    @Query("SELECT * FROM purchase_transactions ORDER BY created_at DESC LIMIT :limit")
    suspend fun getRecentTransactions(limit: Int = 50): List<PurchaseTransactionEntity>
    
    @Query("SELECT * FROM purchase_transactions ORDER BY created_at DESC")
    fun getAllTransactionsFlow(): Flow<List<PurchaseTransactionEntity>>
}