package com.brainwallet.ui.composable.bottomsheet

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.tools.sqlite.CurrencyDataSource
import com.brainwallet.ui.composable.BrainwalletBottomSheet
import com.brainwallet.ui.theme.BrainwalletTheme
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip

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
    val unselectedCircleSize = 20
    val tinyPad = 2


    BrainwalletBottomSheet(
        onDismissRequest = onDismissRequest,
    ) {
        LazyColumn {
            val currencies = CurrencyDataSource.getInstance(context).getAllCurrencies(true)
            items(
                items = currencies
            ) { currency ->
                ListItem(colors = ListItemDefaults.colors(
                    containerColor = BrainwalletTheme.colors.background,
                    headlineColor = BrainwalletTheme.colors.content,
                ), modifier = Modifier.clickable {
                    onFiatSelect.invoke(currency)
                }, headlineContent = {
                    Row {
                        Text(
                            modifier = Modifier.padding(tinyPad.dp),
                            text = currency.name,
                            style = MaterialTheme.typography.labelLarge
                                .copy(textAlign = TextAlign.Left)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            modifier = Modifier.padding(tinyPad.dp),
                            text = "${currency.code} (${currency.symbol})",
                            style = MaterialTheme.typography.labelLarge
                                .copy(textAlign = TextAlign.Left)
                        )
                    }
                }, trailingContent = {
                    if (selectedCurrency.code == currency.code) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = BrainwalletTheme.colors.affirm
                        )
                    } else {
                    Box(
                        modifier = Modifier
                            .size(unselectedCircleSize.dp)
                            .alpha(0.1f)
                            .clip(CircleShape)
                            .background(BrainwalletTheme.colors.content)
                    )
                }
                })
            }
        }
    }
}