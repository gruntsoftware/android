package com.brainwallet.ui.screens.settings.settingsrows

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.brainwallet.ui.screens.settings.SettingsEvent
import com.brainwallet.ui.theme.DesignTheme
import com.grunt.brainwallet.iap.trustednode.TrustedLTCNodeSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LitecoinBlockchainDetail(
    modifier: Modifier = Modifier,
    selectedCurrency: CurrencyEntity,
    selectedFeeType: String,
    feeOptions: List<FeeOption>,
    trustedNodeAddress: String?,
    onEvent: (SettingsEvent) -> Unit,
) {
    // / Layout values
    val contentHeight = 65
    val horizontalPadding = 14

    val trustedNodeLabel = trustedNodeAddress ?: stringResource(R.string.set_node_ip_address)
    val paywallSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val addressEntrySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var trustedNodeStep by remember { mutableStateOf(TrustedNodeStep.None) }

    SettingRowItemExpandable(
        modifier = modifier,
        title = stringResource(R.string.blockchain_litecoin)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = horizontalPadding.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .height(contentHeight.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.trusted_ltc_node))
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        // An address is only ever set after the feature is purchased, so a
                        // set label means "already unlocked" — go straight to editing it;
                        // otherwise start with the paywall.
                        trustedNodeStep = if (trustedNodeAddress != null) {
                            TrustedNodeStep.AddressEntry
                        } else {
                            TrustedNodeStep.Paywall
                        }
                    }
                ) {
                    Text(trustedNodeLabel)
                }
            }
            HorizontalDivider(color = DesignTheme.colors.content)
            Row(
                modifier = Modifier.height(contentHeight.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.settings_blockchain_litecoin_description),
                    modifier = Modifier.weight(1.2f)
                )
                Button(
                    modifier = Modifier.weight(.6f),
                    onClick = {
                        onEvent.invoke(SettingsEvent.OnBlockchainSyncClick)
                    }
                ) {
                    Text(stringResource(R.string.settings_blockchain_litecoin_button))
                }
            }

            HorizontalDivider(color = DesignTheme.colors.content)

            NetworkFeeSelector(
                selectedCurrency = selectedCurrency,
                feeOptions = feeOptions,
                selectedIndex = feeOptions.getSelectedIndex(selectedFeeType)
            ) { newSelectedIndex ->
                onEvent.invoke(SettingsEvent.OnFeeTypeChange(feeOptions[newSelectedIndex].type))
            }
        }

        when (trustedNodeStep) {
            TrustedNodeStep.None -> Unit

            TrustedNodeStep.Paywall -> ModalBottomSheet(
                sheetState = paywallSheetState,
                onDismissRequest = { trustedNodeStep = TrustedNodeStep.None }
            ) {
                TrustedLTCNodeSheet(
                    onPurchased = {
                        onEvent.invoke(SettingsEvent.OnTrustedNodePurchased)
                        // Swap the paywall for the address-entry step.
                        trustedNodeStep = TrustedNodeStep.AddressEntry
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            TrustedNodeStep.AddressEntry -> ModalBottomSheet(
                sheetState = addressEntrySheetState,
                onDismissRequest = { trustedNodeStep = TrustedNodeStep.None }
            ) {
                SetTrustedNodeSheet(
                    currentAddress = trustedNodeAddress,
                    onSubmit = { address ->
                        onEvent.invoke(SettingsEvent.OnTrustedNodeAddressSubmitted(address))
                        trustedNodeStep = TrustedNodeStep.None
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private enum class TrustedNodeStep { None, Paywall, AddressEntry }

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
                        text = feeOptions[index].getFiatFormatted(selectedCurrency), // fiat?
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
