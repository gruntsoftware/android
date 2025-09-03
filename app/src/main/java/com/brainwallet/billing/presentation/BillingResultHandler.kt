package com.brainwallet.billing.presentation

import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.brainwallet.billing.data.model.BrainWalletBillingResult
import com.brainwallet.billing.data.repository.BillingRepository
import com.brainwallet.billing.data.utils.BillingResultFlow
import com.brainwallet.billing.domain.usecase.BillingUseCase
import com.brainwallet.billing.util.PurchaseAcknowledgmentHelper
import com.brainwallet.ui.BrainwalletViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

data class BillingResultState(
    val lastPurchaseResult: BrainWalletBillingResult? = null,
    val pendingAcknowledgments: List<String> = emptyList(),
    val isProcessing: Boolean = false
)

sealed class BillingResultEvent {
    object StartListening : BillingResultEvent()
    object StopListening : BillingResultEvent()
    data class ProcessPurchaseResult(val result: BrainWalletBillingResult) : BillingResultEvent()
}

@KoinViewModel
class BillingResultHandler(
    private val billingResultFlow: BillingResultFlow,
    private val repository: BillingRepository,
    private val applicationContext: android.content.Context
) : BrainwalletViewModel<BillingResultEvent>() {

    private val _state = MutableStateFlow(BillingResultState())
    val state: StateFlow<BillingResultState> = _state.asStateFlow()

    override fun onEvent(event: BillingResultEvent) {
        when (event) {
            is BillingResultEvent.StartListening -> startListening()
            is BillingResultEvent.StopListening -> stopListening()
            is BillingResultEvent.ProcessPurchaseResult -> processPurchaseResult(event.result)
        }
    }

    private fun startListening() {
        viewModelScope.launch {
            billingResultFlow.collect { result ->
                onEvent(BillingResultEvent.ProcessPurchaseResult(result))
            }
        }
    }

    private fun stopListening() {
        // Flow collection will be cancelled when viewModelScope is cancelled
    }

    private fun processPurchaseResult(result: BrainWalletBillingResult) {
        viewModelScope.launch {
            _state.update { it.copy(lastPurchaseResult = result, isProcessing = true) }
            
            try {
                when (result.billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        result.purchases?.let { purchases ->
                            handleSuccessfulPurchases(purchases.filterNotNull())
                        }
                    }
                    
                    BillingClient.BillingResponseCode.USER_CANCELED -> {
                        // User cancelled, no action needed
                    }
                    
                    BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                        // Handle already owned items
                        handleAlreadyOwnedItems()
                    }
                    
                    else -> {
                        // Handle other error cases
                        handlePurchaseError(result)
                    }
                }
            } catch (e: Exception) {
                handleError(e)
            } finally {
                _state.update { it.copy(isProcessing = false) }
            }
        }
    }

    private suspend fun handleSuccessfulPurchases(purchases: List<Purchase>) {
        val pendingTokens = mutableListOf<String>()
        
        purchases.forEach { purchase ->
            when (purchase.purchaseState) {
                Purchase.PurchaseState.PURCHASED -> {
                    // Save purchase to local database
                    try {
                        // Determine product type - you might need to track this differently
                        val productType = if (purchase.products.any { it.contains("sub") }) {
                            BillingClient.ProductType.SUBS
                        } else {
                            BillingClient.ProductType.INAPP
                        }
                        
                        repository.savePurchaseTransaction(purchase, productType)
                        
                        if (!purchase.isAcknowledged) {
                            // Schedule immediate acknowledgment work
                            PurchaseAcknowledgmentHelper.scheduleImmediatePurchaseAcknowledgment(applicationContext)
                            pendingTokens.add(purchase.purchaseToken)
                        }
                    } catch (e: Exception) {
                        handleError(e)
                    }
                }
                
                Purchase.PurchaseState.PENDING -> {
                    // Purchase is pending (e.g., waiting for parental approval)
                    // Save to database but don't schedule acknowledgment yet
                    try {
                        val productType = if (purchase.products.any { it.contains("sub") }) {
                            BillingClient.ProductType.SUBS
                        } else {
                            BillingClient.ProductType.INAPP
                        }
                        
                        repository.savePurchaseTransaction(purchase, productType)
                    } catch (e: Exception) {
                        handleError(e)
                    }
                }
                
                else -> {
                    // Handle other states if needed
                }
            }
        }
        
        _state.update { 
            it.copy(pendingAcknowledgments = it.pendingAcknowledgments + pendingTokens) 
        }
    }

    private suspend fun handleAlreadyOwnedItems() {
        // Query current purchases to refresh the cache
        try {
            val inAppResult = repository.queryUserPurchases(BillingClient.ProductType.INAPP)
            val subsResult = repository.queryUserPurchases(BillingClient.ProductType.SUBS)
            
            // Save all purchases to database and process any unacknowledged ones
            val inAppPurchases = inAppResult.getOrNull() ?: emptyList()
            val subsPurchases = subsResult.getOrNull() ?: emptyList()
            
            // Save to database
            if (inAppPurchases.isNotEmpty()) {
                repository.savePurchaseTransactions(inAppPurchases, BillingClient.ProductType.INAPP)
            }
            if (subsPurchases.isNotEmpty()) {
                repository.savePurchaseTransactions(subsPurchases, BillingClient.ProductType.SUBS)
            }
            
            // Process any unacknowledged purchases
            val allPurchases = inAppPurchases + subsPurchases
            handleSuccessfulPurchases(allPurchases)
        } catch (e: Exception) {
            handleError(e)
        }
    }

    private fun handlePurchaseError(result: BrainWalletBillingResult) {
        val errorMessage = when (result.billingResult.responseCode) {
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> 
                "Billing is unavailable on this device"
            
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> 
                "Developer error occurred during purchase"
            
            BillingClient.BillingResponseCode.ERROR -> 
                "An internal error occurred"
            
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> 
                "This billing feature is not supported"
            
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> 
                "Billing service disconnected"
            
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> 
                "Billing service is temporarily unavailable"
            
            BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> 
                "Purchase request timed out"
            
            BillingClient.BillingResponseCode.NETWORK_ERROR -> 
                "Network error occurred during purchase"
            
            else -> "Purchase failed: ${result.billingResult.debugMessage ?: "Unknown error"}"
        }
        
        handleError(Exception(errorMessage))
    }

    fun removePendingAcknowledgment(purchaseToken: String) {
        _state.update { 
            it.copy(
                pendingAcknowledgments = it.pendingAcknowledgments.filter { token -> 
                    token != purchaseToken 
                }
            ) 
        }
    }


}