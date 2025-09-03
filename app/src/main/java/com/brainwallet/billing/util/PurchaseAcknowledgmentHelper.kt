package com.brainwallet.billing.util

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.brainwallet.billing.worker.PurchaseAcknowledgmentWorker
import java.util.concurrent.TimeUnit

object PurchaseAcknowledgmentHelper {

    private const val PERIODIC_WORK_NAME = "periodic_purchase_acknowledgment"
    private const val IMMEDIATE_WORK_NAME = "immediate_purchase_acknowledgment"
    
    /**
     * Schedules immediate acknowledgment work for new purchases
     */
    fun scheduleImmediatePurchaseAcknowledgment(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<PurchaseAcknowledgmentWorker>()
            .setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                IMMEDIATE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
    }

    /**
     * Schedules periodic acknowledgment work to handle any missed purchases
     */
    fun schedulePeriodicPurchaseAcknowledgment(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val periodicWorkRequest = PeriodicWorkRequestBuilder<PurchaseAcknowledgmentWorker>(
            repeatInterval = 15, // Every 15 minutes
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
            flexTimeInterval = 5, // 5 minute flex window
            flexTimeIntervalUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWorkRequest
            )
    }

    /**
     * Cancels all purchase acknowledgment work
     */
    fun cancelAllPurchaseAcknowledgmentWork(context: Context) {
        val workManager = WorkManager.getInstance(context)
        workManager.cancelUniqueWork(IMMEDIATE_WORK_NAME)
        workManager.cancelUniqueWork(PERIODIC_WORK_NAME)
    }

    /**
     * Cancels periodic work only
     */
    fun cancelPeriodicPurchaseAcknowledgment(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_WORK_NAME)
    }
}