package com.brainwallet.billing.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.brainwallet.billing.domain.usecase.BillingUseCase
import com.brainwallet.billing.data.repository.BillingRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class PurchaseAcknowledgmentWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), KoinComponent {

    private val billingUseCase: BillingUseCase by inject()
    private val billingRepository: BillingRepository by inject()

    companion object {
        const val WORK_NAME = "purchase_acknowledgment_work"
    }

    override suspend fun doWork(): Result {
        return try {
            // Get all purchases that are ready for acknowledgment from local database
            val purchasesToAcknowledge = billingRepository.getPurchasesReadyForAcknowledgment()
            
            if (purchasesToAcknowledge.isEmpty()) {
                return Result.success(createSuccessData("No purchases to acknowledge"))
            }

            var successCount = 0
            var failureCount = 0
            val errors = mutableListOf<String>()

            purchasesToAcknowledge.forEach { purchaseTransaction ->
                try {
                    // Increment attempt count first
                    billingRepository.incrementAcknowledgmentAttempt(purchaseTransaction.purchaseToken)
                    
                    // Attempt to acknowledge the purchase
                    val result = billingRepository.acknowledgePurchaseWithRetry(purchaseTransaction.purchaseToken)
                    
                    if (result.isSuccessful) {
                        // Mark as acknowledged in local database
                        billingRepository.markPurchaseAcknowledged(purchaseTransaction.purchaseToken)
                        successCount++
                    } else {
                        failureCount++
                        errors.add("${purchaseTransaction.purchaseToken}: ${result.errorMessage}")
                        
                        // If this purchase has exceeded max attempts, we might want to handle it differently
                        if (purchaseTransaction.acknowledgmentAttempts >= 4) { // 5 total attempts (0-4)
                            // Log or handle permanently failed acknowledgments
                            errors.add("Purchase ${purchaseTransaction.purchaseToken} exceeded max attempts")
                        }
                    }
                } catch (e: Exception) {
                    failureCount++
                    errors.add("${purchaseTransaction.purchaseToken}: ${e.message}")
                }
            }

            // Determine result based on success/failure ratio
            return when {
                successCount > 0 && failureCount == 0 -> {
                    Result.success(createSuccessData("Successfully acknowledged $successCount purchases"))
                }
                
                successCount > 0 && failureCount > 0 -> {
                    // Partial success - schedule retry for failed ones
                    Result.success(createPartialSuccessData(successCount, failureCount, errors))
                }
                
                failureCount > 0 -> {
                    // All failed, but we should retry later
                    Result.retry()
                }
                
                else -> {
                    Result.success(createSuccessData("No purchases processed"))
                }
            }
        } catch (e: Exception) {
            Result.failure(createErrorData("Worker failed with exception: ${e.message}"))
        }
    }

    private fun createSuccessData(message: String): Data {
        return Data.Builder()
            .putString("result", "success")
            .putString("message", message)
            .build()
    }

    private fun createPartialSuccessData(successCount: Int, failureCount: Int, errors: List<String>): Data {
        return Data.Builder()
            .putString("result", "partial_success")
            .putString("message", "Acknowledged $successCount purchases, $failureCount failed")
            .putInt("success_count", successCount)
            .putInt("failure_count", failureCount)
            .putStringArray("errors", errors.toTypedArray())
            .build()
    }

    private fun createErrorData(message: String): Data {
        return Data.Builder()
            .putString("result", "error")
            .putString("message", message)
            .build()
    }
}