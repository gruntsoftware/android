package com.brainwallet.ui.screens.settings.settingsrows

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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.brainwallet.R
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.GlobalCurrency
import com.brainwallet.ui.theme.DesignTheme

@Composable
fun CurrencyDetail(
    modifier: Modifier = Modifier,
    selectedCurrency: CurrencyEntity,
    onFiatSelect: (CurrencyEntity) -> Unit
) {
    val context = LocalContext.current

    // Layout values
    val expandedHeight = 300
    val unselectedCircleSize = 20
    val tinyPad = 2

    val globalCurrencies = remember { GlobalCurrency.entries }
    val listState = rememberLazyListState()
    val selectedIndex = remember(selectedCurrency) {
        globalCurrencies.indexOfFirst { it.code == selectedCurrency.code }
    }

    LaunchedEffect(selectedIndex) {
        if (selectedIndex >= 0) {
            listState.scrollToItem(selectedIndex)
        }
    }

    SettingRowItemExpandable(
        modifier = modifier,
        title = stringResource(R.string.settings_title_currency)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.height(expandedHeight.dp)
        ) {
            items(items = globalCurrencies) { currency ->
                val currencyEntity = CurrencyEntity()
                currencyEntity.code = currency.code
                currencyEntity.name = currency.fullCurrencyName
                currencyEntity.rate = 1f
                currencyEntity.symbol = currency.symbol

                ListItem(
                    colors = ListItemDefaults.colors(
                        containerColor = DesignTheme.colors.background,
                        headlineColor = DesignTheme.colors.content,
                    ),
                    modifier = Modifier
                        .height(44.dp)
                        .clickable {
                            onFiatSelect.invoke(currencyEntity)
                        },
                    headlineContent = {
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
                    },
                    trailingContent = {
                        if (selectedCurrency.code == currency.code) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = DesignTheme.colors.affirm
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(unselectedCircleSize.dp)
                                    .alpha(0.1f)
                                    .clip(CircleShape)
                                    .background(DesignTheme.colors.content)
                            )
                        }
                    }
                )
            }
        }
    }
}
