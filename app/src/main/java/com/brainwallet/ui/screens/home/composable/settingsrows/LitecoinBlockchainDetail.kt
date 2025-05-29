package com.brainwallet.ui.screens.home.composable.settingsrows

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.R
import com.brainwallet.data.model.CurrencyEntity
import com.brainwallet.data.model.FeeOption
import com.brainwallet.data.model.getFiatFormatted
import com.brainwallet.data.model.getSelectedIndex
import com.brainwallet.ui.screens.home.SettingsEvent
import com.brainwallet.ui.theme.BrainwalletTheme

//TODO
@Composable
fun LitecoinBlockchainDetail(
    modifier: Modifier = Modifier,
    selectedCurrency: CurrencyEntity,
    selectedFeeType: String,
    feeOptions: List<FeeOption>,
    onEvent: (SettingsEvent) -> Unit,
) {

    /// Layout values
    val contentHeight = 60
    val horizontalPadding = 14

    SettingRowItemExpandable(
        modifier = modifier,
        title = stringResource(R.string.blockchain_litecoin)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = horizontalPadding.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {

            Row(
                modifier = Modifier.height(contentHeight.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.settings_blockchain_litecoin_description),
                    modifier = Modifier.weight(1.3f)
                )
                Button(
                    modifier = Modifier.weight(.7f),
                    onClick = {
                        onEvent.invoke(SettingsEvent.OnBlockchainSyncClick)
                    }
                ) {
                    Text(stringResource(R.string.settings_blockchain_litecoin_button))
                }
            }

            HorizontalDivider(color = BrainwalletTheme.colors.content)

            NetworkFeeSelector(
                selectedCurrency = selectedCurrency,
                feeOptions = feeOptions,
                selectedIndex = feeOptions.getSelectedIndex(selectedFeeType)
            ) { newSelectedIndex ->
                onEvent.invoke(SettingsEvent.OnFeeTypeChange(feeOptions[newSelectedIndex].type))
            }

        }
    }
}

@Composable
private fun NetworkFeeSelector(
    selectedCurrency: CurrencyEntity,
    feeOptions: List<FeeOption>,
    selectedIndex: Int,
    onSelectedChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.network_fee_options_desc),
            fontSize = 12.sp,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            feeOptions.forEachIndexed { index, feeOption ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${feeOption.feePerKb}",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = feeOptions[index].getFiatFormatted(selectedCurrency), //fiat?
                        fontSize = 12.sp
                    )
                }
            }
        }

        SingleChoiceSegmentedButtonRow {
            feeOptions.forEachIndexed { index, feeOption ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = feeOptions.size,
                        baseShape = MaterialTheme.shapes.extraLarge
                    ),
                    onClick = { onSelectedChange.invoke(index) },
                    selected = index == selectedIndex,
                    label = { Text(stringResource(feeOption.labelStringId)) }
                )
            }
        }


    }
}
