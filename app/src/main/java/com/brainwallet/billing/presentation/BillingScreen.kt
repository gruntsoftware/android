@file:OptIn(ExperimentalMaterial3Api::class)

package com.brainwallet.billing.presentation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.ProductDetails
import com.brainwallet.billing.domain.model.ProductQueryResult
import com.brainwallet.billing.domain.usecase.BillingUseCase
import com.brainwallet.data.model.AppSetting
import com.brainwallet.navigation.OnNavigate
import com.brainwallet.navigation.UiEffect
import androidx.lifecycle.viewModelScope
import com.brainwallet.billing.data.repository.BillingRepository
import com.brainwallet.ui.BrainwalletViewModel
import com.brainwallet.ui.composable.BrainwalletScaffold
import com.brainwallet.ui.theme.BrainwalletAppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel
import org.koin.compose.viewmodel.koinViewModel

data class BillingScreenState(
    val products: List<ProductDetails> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showPurchaseDialog: Boolean = false,
    val selectedProduct: ProductDetails? = null
)

sealed class BillingScreenEvent {
    object LoadProducts : BillingScreenEvent()
    data class ShowPurchaseDialog(val product: ProductDetails) : BillingScreenEvent()
    object HidePurchaseDialog : BillingScreenEvent()
    object PurchaseCompleted : BillingScreenEvent()
}

@KoinViewModel
class BillingScreenViewModel(
    private val billingRepository: BillingRepository
) : BrainwalletViewModel<BillingScreenEvent>() {

    private val _state = MutableStateFlow(BillingScreenState())
    val state: StateFlow<BillingScreenState> = _state.asStateFlow()

    override fun onEvent(event: BillingScreenEvent) {
        when (event) {
            is BillingScreenEvent.LoadProducts -> loadProducts()
            is BillingScreenEvent.ShowPurchaseDialog -> {
                _state.update { 
                    it.copy(
                        showPurchaseDialog = true, 
                        selectedProduct = event.product
                    ) 
                }
            }
            is BillingScreenEvent.HidePurchaseDialog -> {
                _state.update { 
                    it.copy(
                        showPurchaseDialog = false, 
                        selectedProduct = null
                    ) 
                }
            }
            is BillingScreenEvent.PurchaseCompleted -> {
                _state.update { 
                    it.copy(
                        showPurchaseDialog = false, 
                        selectedProduct = null
                    ) 
                }
                // Reload products to reflect changes
                loadProducts()
            }
        }
    }

    private fun loadProducts() {
        onLoading(true, "Loading products...")
        _state.update { it.copy(isLoading = true, errorMessage = null) }
        
        // Example product IDs - replace with your actual product IDs
        val productIds = listOf("premium_features", "remove_ads", "extra_storage")
        
        viewModelScope.launch {
            try {
                val result = billingRepository.queryAvailableProducts(
                    productIds = productIds,
                    productType = BillingClient.ProductType.INAPP
                )
                
                if (result.errorMessage != null) {
                    _state.update { 
                        it.copy(
                            isLoading = false, 
                            errorMessage = result.errorMessage
                        ) 
                    }
                    handleError(Exception(result.errorMessage))
                } else {
                    _state.update { 
                        it.copy(
                            isLoading = false, 
                            products = result.products
                        ) 
                    }
                }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false, 
                        errorMessage = e.message
                    ) 
                }
                handleError(e)
            } finally {
                onLoading(false)
            }
        }
    }
}

@Composable
fun BillingScreen(
    onNavigate: OnNavigate,
    modifier: Modifier = Modifier,
    viewModel: BillingScreenViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val loadingState by viewModel.loadingState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.onEvent(BillingScreenEvent.LoadProducts)
        
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is UiEffect.Navigate -> onNavigate.invoke(effect)
                else -> Unit
            }
        }
    }

    BillingScreen(
        state = state,
        isLoading = loadingState.visible,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
    
    // Show purchase dialog
    if (state.showPurchaseDialog && state.selectedProduct != null) {
        PurchaseFlowDialog(
            productDetails = state.selectedProduct!!,
            onDismiss = { 
                viewModel.onEvent(BillingScreenEvent.HidePurchaseDialog) 
            },
            onPurchaseSuccess = { 
                viewModel.onEvent(BillingScreenEvent.PurchaseCompleted) 
            },
            onPurchaseError = { errorMessage ->
                // Error is already handled by the ViewModel's error handling
                viewModel.onEvent(BillingScreenEvent.HidePurchaseDialog)
            }
        )
    }
}

@Composable
private fun BillingScreen(
    state: BillingScreenState,
    isLoading: Boolean,
    onEvent: (BillingScreenEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    BrainwalletScaffold(
        modifier = modifier,
        topBar = {
            Text(
                text = "Premium Features",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.products.isEmpty() && !isLoading) {
                Text(
                    text = "No products available",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Button(
                    onClick = { onEvent(BillingScreenEvent.LoadProducts) }
                ) {
                    Text("Retry")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.products) { product ->
                        ProductCard(
                            product = product,
                            onPurchaseClick = { 
                                onEvent(BillingScreenEvent.ShowPurchaseDialog(product)) 
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: ProductDetails,
    onPurchaseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        onClick = onPurchaseClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium
            )
            
            Text(
                text = product.description,
                style = MaterialTheme.typography.bodyMedium
            )
            
            val price = product.oneTimePurchaseOfferDetails?.formattedPrice ?: "Price not available"
            Text(
                text = price,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun BillingScreenPreview() {
    BrainwalletAppTheme(appSetting = AppSetting(isDarkMode = isSystemInDarkTheme())) {
        BillingScreen(
            state = BillingScreenState(
                products = emptyList(),
                isLoading = false
            ),
            isLoading = false,
            onEvent = {}
        )
    }
}