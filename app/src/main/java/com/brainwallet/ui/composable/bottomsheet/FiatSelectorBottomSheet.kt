package com.brainwallet.ui.composable.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.tools.sqlite.CurrencyDataSource
import com.brainwallet.ui.composable.BrainwalletBottomSheet
import com.brainwallet.ui.theme.BrainwalletTheme

/**
 * describe [FiatSelectorBottomSheet] for CurrencySelector
 */
@Composable
fun FiatSelectorBottomSheet(
    selectedCurrency: CurrencyEntity,
    onDismissRequest: () -> Unit,
    onFiatSelect: (CurrencyEntity) -> Unit
) {
    val context = LocalContext.current
    BrainwalletBottomSheet(
        onDismissRequest = onDismissRequest,
    ) {
        LazyColumn {
            val currencies = CurrencyDataSource.getInstance(context).allCurrencies

            items(
                items = currencies
            ) { currency ->
                ListItem(colors = ListItemDefaults.colors(
                    containerColor = BrainwalletTheme.colors.background,
                    headlineColor = BrainwalletTheme.colors.content,
                ), modifier = Modifier.clickable {
                    onFiatSelect.invoke(currency)
                }, headlineContent = {
                    Text(currency.name)
                }, trailingContent = {
                    if (selectedCurrency.code == currency.code) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = BrainwalletTheme.colors.affirm
                        )
                    }
                })
            }
        }
    }
}