package com.brainwallet.billing.presentation

import android.app.Activity
import androidx.lifecycle.viewModelScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.brainwallet.billing.data.repository.BillingRepository
import com.brainwallet.billing.data.source.remote.BillingRemoteSource
import com.brainwallet.billing.domain.model.PurchaseFlowState
import com.brainwallet.billing.domain.model.PurchaseResult
import com.brainwallet.ui.BrainwalletViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

sealed class PurchaseFlowEvent {
    data class InitiatePurchase(
        val activity: Activity,
        val productDetails: ProductDetails,
        val offerToken: String? = null
    ) : PurchaseFlowEvent()
    
    object ClearResult : PurchaseFlowEvent()
}

@KoinViewModel
class PurchaseFlowViewModel(
    private val repository: BillingRepository,
    private val billingRemoteSource: BillingRemoteSource
) : BrainwalletViewModel<PurchaseFlowEvent>() {

    private val _state = MutableStateFlow(PurchaseFlowState())
    val state: StateFlow<PurchaseFlowState> = _state.asStateFlow()

    override fun onEvent(event: PurchaseFlowEvent) {
        when (event) {
            is PurchaseFlowEvent.InitiatePurchase -> {
                initiatePurchaseFlow(event.activity, event.productDetails, event.offerToken)
            }
            is PurchaseFlowEvent.ClearResult -> {
                clearPurchaseResult()
            }
        }
    }

    private fun initiatePurchaseFlow(activity: Activity, productDetails: ProductDetails, offerToken: String? = null) {
        viewModelScope.launch {
            onLoading(true, "Initiating purchase...")
            _state.update { it.copy(isLoading = true, errorMessage = null, purchaseResult = null) }
            
            try {
                // Ensure billing is ready
                if (!repository.isReady()) {
                    val connectionResult = repository.establishConnection()
                    if (!connectionResult.isConnected) {
                        val errorMessage = "Failed to establish connection: ${connectionResult.errorMessage}"
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                purchaseResult = PurchaseResult(
                                    isSuccessful = false,
                                    errorMessage = errorMessage
                                )
                            )
                        }
                        handleError(Exception(errorMessage))
                        onLoading(false)
                        return@launch
                    }
                }

                val productDetailsParamsList = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .apply { 
                            offerToken?.let { setOfferToken(it) }
                        }
                        .build()
                )

                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()

                val billingResult = billingRemoteSource.launchBillingFlow(activity, billingFlowParams)
                
                val purchaseResult = when (billingResult.responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        PurchaseResult(
                            isSuccessful = true,
                            errorMessage = "Purchase flow initiated successfully"
                        )
                    }
                    
                    BillingClient.BillingResponseCode.USER_CANCELED -> {
                        PurchaseResult(
                            isSuccessful = false,
                            errorMessage = "Purchase was cancelled by user",
                            userCancelled = true
                        )
                    }
                    
                    BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                        PurchaseResult(
                            isSuccessful = false,
                            errorMessage = "Item is already owned by the user"
                        )
                    }
                    
                    BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> {
                        PurchaseResult(
                            isSuccessful = false,
                            errorMessage = "The requested item is not available for purchase"
                        )
                    }
                    
                    else -> {
                        PurchaseResult(
                            isSuccessful = false,
                            errorMessage = handlePurchaseFlowError(billingResult)
                        )
                    }
                }
                
                _state.update { 
                    it.copy(
                        isLoading = false,
                        purchaseResult = purchaseResult
                    )
                }
                
                if (!purchaseResult.isSuccessful && !purchaseResult.userCancelled) {
                    handleError(Exception(purchaseResult.errorMessage))
                }
                
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        purchaseResult = PurchaseResult(
                            isSuccessful = false,
                            errorMessage = "Failed to initiate purchase flow: ${e.message}"
                        )
                    )
                }
                handleError(e)
            } finally {
                onLoading(false)
            }
        }
    }

    private fun clearPurchaseResult() {
        _state.update { it.copy(purchaseResult = null, errorMessage = null) }
    }

    private fun handlePurchaseFlowError(billingResult: BillingResult): String {
        return when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> 
                "Billing is unavailable on this device. Please check your Google Play Store setup."
            
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> 
                "Developer error occurred. Please check the purchase configuration."
            
            BillingClient.BillingResponseCode.ERROR -> 
                "An internal error occurred during the purchase flow."
            
            BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED -> 
                "This billing feature is not supported on this device."
            
            BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> 
                "Connection to Google Play Billing service was lost during purchase."
            
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> 
                "Google Play Billing service is temporarily unavailable."
            
            BillingClient.BillingResponseCode.SERVICE_TIMEOUT -> 
                "The purchase request timed out. Please try again."
            
            BillingClient.BillingResponseCode.NETWORK_ERROR -> 
                "Network error occurred during purchase. Please check your connection."
            
            else -> "Purchase failed: ${billingResult.debugMessage ?: "Unknown error"}"
        }
    }
}