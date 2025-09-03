package com.brainwallet.billing.data.source.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.brainwallet.billing.data.source.local.dao.PurchaseTransactionDao
import com.brainwallet.billing.data.source.local.entity.PurchaseTransactionEntity

@Database(
    entities = [PurchaseTransactionEntity::class],
    version = 1,
    exportSchema = true
)
abstract class BillingDatabase : RoomDatabase() {
    
    abstract fun purchaseTransactionDao(): PurchaseTransactionDao
    
    companion object {
        const val DATABASE_NAME = "billing_database"
        
        // Migration example for future versions
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Example migration - add new column
                // database.execSQL("ALTER TABLE purchase_transactions ADD COLUMN new_column TEXT")
            }
        }
        
        fun create(context: Context): BillingDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                BillingDatabase::class.java,
                DATABASE_NAME
            )
            .addMigrations(MIGRATION_1_2)
            .build()
        }
    }
}