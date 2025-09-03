@file:OptIn(ExperimentalMaterial3Api::class)

package com.brainwallet.billing.presentation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.android.billingclient.api.ProductDetails
import com.brainwallet.billing.domain.model.PurchaseFlowState
import com.brainwallet.billing.domain.model.PurchaseResult
import com.brainwallet.data.model.AppSetting
import com.brainwallet.navigation.UiEffect
import com.brainwallet.ui.theme.BrainwalletAppTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PurchaseFlowDialog(
    productDetails: ProductDetails,
    modifier: Modifier = Modifier,
    offerToken: String? = null,
    onDismiss: () -> Unit,
    onPurchaseSuccess: () -> Unit,
    onPurchaseError: (String) -> Unit,
    viewModel: PurchaseFlowViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val loadingState by viewModel.loadingState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collect { effect ->
            when (effect) {
                is UiEffect.ShowMessage -> {
                    if (effect.type == UiEffect.ShowMessage.Type.Error) {
                        onPurchaseError(effect.message)
                    }
                }

                else -> Unit
            }
        }
    }

    LaunchedEffect(state.purchaseResult) {
        state.purchaseResult?.let { result ->
            if (result.isSuccessful) {
                onPurchaseSuccess()
            } else if (!result.userCancelled) {
                onPurchaseError(result.errorMessage ?: "Unknown error")
            }
            viewModel.onEvent(PurchaseFlowEvent.ClearResult)
        }
    }

    PurchaseFlowDialog(
        productDetails = productDetails,
        offerToken = offerToken,
        state = state,
        isLoading = loadingState.visible,
        onDismiss = onDismiss,
        onPurchase = {
            viewModel.onEvent(
                PurchaseFlowEvent.InitiatePurchase(
                    activity = context as ComponentActivity,
                    productDetails = productDetails,
                    offerToken = offerToken
                )
            )
        },
        modifier = modifier
    )
}

@Composable
private fun PurchaseFlowDialog(
    productDetails: ProductDetails,
    offerToken: String?,
    state: PurchaseFlowState,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onPurchase: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Purchase ${productDetails.name}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = productDetails.description,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Show price information
                val priceInfo = productDetails.oneTimePurchaseOfferDetails?.formattedPrice
                    ?: productDetails.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
                    ?: "Price not available"

                Text(
                    text = priceInfo,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (isLoading || state.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("Processing purchase...")
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }

                        Button(
                            onClick = onPurchase,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Purchase")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PurchaseErrorDialog(
    errorMessage: String,
    onDismiss: () -> Unit,
    onRetry: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Purchase Failed")
        },
        text = {
            Text(errorMessage)
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        },
        dismissButton = onRetry?.let {
            {
                TextButton(onClick = it) {
                    Text("Retry")
                }
            }
        }
    )
}

@Composable
fun PurchaseSuccessDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Purchase Successful")
        },
        text = {
            Text("Your purchase has been completed successfully!")
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@PreviewLightDark
@Composable
private fun PurchaseFlowDialogPreview() {
    BrainwalletAppTheme(appSetting = AppSetting(isDarkMode = isSystemInDarkTheme())) {
//        PurchaseFlowDialog(
//            productDetails = mockProductDetails,
//            offerToken = null,
//            state = PurchaseFlowState(isLoading = false),
//            isLoading = false,
//            onDismiss = {},
//            onPurchase = {}
//        )
    }
}