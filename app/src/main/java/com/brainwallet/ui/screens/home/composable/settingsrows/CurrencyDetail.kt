package com.brainwallet.ui.screens.home.composable.settingsrows

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.brainwallet.R
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.tools.sqlite.CurrencyDataSource
import com.brainwallet.ui.theme.BrainwalletTheme

@Composable
fun CurrencyDetail(
    modifier: Modifier = Modifier,
    selectedCurrency: CurrencyEntity,
    onFiatSelect: (CurrencyEntity) -> Unit
) {
    val context = LocalContext.current

    /// Layout values
    val closedHeight = 60
    val expandedHeight = 100
    val dividerThickness = 1
    val unselectedCircleSize = 20
    val tinyPad = 2

    SettingRowItemExpandable(
        modifier = modifier,
        title = stringResource(R.string.settings_title_currency)
    ) {
        LazyColumn(
            modifier = Modifier.height(expandedHeight.dp)
        ) {
            items(
                items = CurrencyDataSource.getInstance(context).getAllCurrencies(true)
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
                            style = MaterialTheme.typography.labelMedium
                                .copy(textAlign = TextAlign.Left)
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            modifier = Modifier.padding(tinyPad.dp),
                            text = "${currency.code} (${currency.symbol})",
                            style = MaterialTheme.typography.labelMedium
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