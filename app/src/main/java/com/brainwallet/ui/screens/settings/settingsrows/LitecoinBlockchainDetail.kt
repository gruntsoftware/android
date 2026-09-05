package com.brainwallet.ui.screens.settings.settingsrows

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.TextStyle
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
import com.brainwallet.ui.theme.IBMPlexSans
import com.grunt.brainwallet.iap.trustednode.TrustedLTCNodeSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LitecoinBlockchainDetail(
    modifier: Modifier = Modifier,
    selectedCurrency: CurrencyEntity,
    selectedFeeType: String,
    feeOptions: List<FeeOption>,
    trustedNodeAddress: String?,
    trustedNodeEntitled: Boolean,
    onEvent: (SettingsEvent) -> Unit,
) {
    // / Layout values
    // No fixed section heights here: the header row above (SettingRowItemExpandable's
    // ListItem) already sizes to its content rather than a fixed dp value, so these
    // sections wrap their content too and rely on vertical padding for rhythm. A fixed
    // height risked clipping text on longer translations or larger accessibility font
    // scales.
    val sectionVerticalPadding = 12

    val horizontalPadding = 14

    val trustedNodeLabel = trustedNodeAddress ?: stringResource(R.string.set_node_ip_address)
    val paywallSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val addressEntrySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var trustedNodeStep by remember { mutableStateOf(TrustedNodeStep.None) }
    var trustedPeerEnabled by remember { mutableStateOf(true) }
    val peerModeButtonLabel = if (trustedPeerEnabled) trustedNodeLabel else stringResource(R.string.mainnet_peer_label)

    SettingRowItemExpandable(
        modifier = modifier,
        title = stringResource(R.string.blockchain_litecoin)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = horizontalPadding.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HorizontalDivider(color = DesignTheme.colors.content)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = sectionVerticalPadding.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    modifier = Modifier.padding(bottom = 4.dp),
                    text = stringResource(R.string.peer_sync_mode),
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    ),
                    maxLines = 1
                )
                Text(
                    modifier = Modifier,
                    text = stringResource(R.string.peer_sync_description),
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp
                    ),
                    maxLines = 2
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.padding(end = 8.dp),
                        text = stringResource(R.string.peer_toggle_label),
                        style = TextStyle(
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        maxLines = 1
                    )
                    Switch(
                        modifier = Modifier.padding(start = 4.dp, end = 4.dp),
                        checked = trustedPeerEnabled,
                        onCheckedChange = { checked ->
                            // Flip the local state so the switch actually reflects the tap;
                            // previously only the event fired and the checked state never
                            // changed, so the switch looked stuck.
                            trustedPeerEnabled = checked
                            onEvent.invoke(SettingsEvent.OnTrustedNodeToggle)
                        }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        modifier = Modifier,
                        enabled = trustedPeerEnabled,
                        // Default M3 button padding (24dp horizontal / 8dp vertical) plus the
                        // label's own padding left too little room in this narrow, unweighted
                        // slot - between the toggle label and the switch it was enough to force
                        // the text to wrap. Tighten it here instead of padding the label itself.
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = DesignTheme.colors.content,
                            disabledContainerColor = DesignTheme.colors.content.copy(alpha = 0.12f),
                            disabledContentColor = DesignTheme.colors.content.copy(alpha = 0.6f)
                        ),
                        onClick = {
                            // Already entitled (RevenueCat) or an address already set → go
                            // straight to editing it; otherwise start with the paywall.
                            trustedNodeStep =
                                if (trustedNodeEntitled || trustedNodeAddress != null) {
                                    TrustedNodeStep.AddressEntry
                                } else {
                                    TrustedNodeStep.Paywall
                                }
                        }
                    ) {
                        Text(
                            text = peerModeButtonLabel,
                            style = TextStyle(
                                fontFamily = IBMPlexSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp
                            ),
                            maxLines = 1
                        )
                    }
                }
            }
            HorizontalDivider(color = DesignTheme.colors.content)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = sectionVerticalPadding.dp),
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
