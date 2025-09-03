package com.brainwallet.billing.data.repository

import com.android.billingclient.api.BillingClient
import com.brainwallet.billing.data.source.remote.BillingException
import com.brainwallet.billing.data.source.remote.BillingRemoteSource
import com.brainwallet.billing.domain.model.ConnectionResult
import com.brainwallet.billing.domain.repository.BillingConnectionRepository

class BillingConnectionRepositoryImpl(
    private val billingRemoteSource: BillingRemoteSource
) : BillingConnectionRepository {

    override suspend fun establishConnection(): ConnectionResult {
        return try {
            val result = billingRemoteSource.establishConnection()
            
            if (result.isSuccess) {
                ConnectionResult(isConnected = true)
            } else {
                val exception = result.exceptionOrNull()
                val errorMessage = when (exception) {
                    is BillingException -> handleConnectionError(exception)
                    else -> "Unknown connection error: ${exception?.message}"
                }
                ConnectionResult(isConnected = false, errorMessage = errorMessage)
            }
        } catch (e: Exception) {
            ConnectionResult(
                isConnected = false, 
                errorMessage = "Unexpected error during connection: ${e.message}"
            )
        }
    }

    override fun isReady(): Boolean {
        return billingRemoteSource.isReady()
    }

    override fun isFeatureSupported(feature: String): Boolean {
        return billingRemoteSource.isFeatureSupported(feature)
    }

    private fun handleConnectionError(exception: BillingException): String {
        return when (exception.responseCode) {
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> 
                "Billing is unavailable. Please check your Google Play Store version and account setup."
            
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> 
                "Google Play Billing service is temporarily unavailable. Please try again later."
            
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> 
                "Connection to Google Play Billing service was lost. Retrying connection..."
            
            BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> 
                "Connection to Google Play Billing service timed out. Please try again."
            
            BillingClient.BillingResponseCode.NETWORK_ERROR -> 
                "Network error occurred. Please check your internet connection and try again."
            
            BillingClient.BillingResponseCode.ERROR -> 
                "An internal error occurred with Google Play Billing. Please try again later."
            
            else -> "Connection failed: ${exception.message}"
        }
    }
}