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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
import com.brainwallet.tools.util.TrustedNode
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
    trustedNodePort: String?,
    trustedNodeEntitled: Boolean,
    // Persisted trusted-node sync preference (BRKeyStore#getTrustedNodeSyncPreference),
    // surfaced through SettingsState#userPrefersTrustedNode. Defaults to false when nothing
    // has been stored, so the toggle below always renders the user's last choice on show.
    trustedPeerEnabled: Boolean,
    onEvent: (SettingsEvent) -> Unit,
) {
    // / Layout values
    val sectionVerticalPadding = 12

    val horizontalPadding = 14

    val trustedNodeLabel = formatTrustedNodeLabel(trustedNodeAddress, trustedNodePort)
        ?: stringResource(R.string.set_node_ip_address)
    val paywallSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val addressEntrySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var trustedNodeStep by remember { mutableStateOf(TrustedNodeStep.None) }

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
                        .padding(8.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Label stacked above its switch (instead of both sitting inline next to
                    // the button) so the switch reads as clearly tied to "Toggle mode" and
                    // the button gets breathing room instead of sitting right against it.
                    Column {
                        Text(
                            modifier = Modifier.padding(bottom = 4.dp),
                            text = stringResource(R.string.peer_toggle_label),
                            style = TextStyle(
                                fontFamily = IBMPlexSans,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            ),
                            maxLines = 1
                        )
                        Switch(
                            checked = trustedPeerEnabled,
                            onCheckedChange = { checked ->
                                onEvent.invoke(SettingsEvent.OnTrustedNodeToggle(checked))
                            }
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(
                        modifier = if (trustedPeerEnabled) {
                            Modifier.shadow(
                                elevation = 4.dp,
                                shape = ButtonDefaults.shape,
                                clip = false,
                                ambientColor = Color.Black.copy(alpha = 0.5f),
                                spotColor = Color.Black.copy(alpha = 0.8f)
                            )
                        } else {
                            Modifier
                        },
                        enabled = trustedPeerEnabled,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            contentColor = Color.White,
                            disabledContainerColor = DesignTheme.colors.content.copy(alpha = 0.12f),
                            disabledContentColor = DesignTheme.colors.content.copy(alpha = 0.6f)
                        ),
                        onClick = {
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
                                fontSize = 13.sp,
                                letterSpacing = if (trustedPeerEnabled) 1.1.sp else 0.0.sp
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
                onDismissRequest = { trustedNodeStep = TrustedNodeStep.None },
                // DesignTheme only themes typography, not MaterialTheme.colorScheme, so the
                // sheet's default containerColor (colorScheme.surface) is always the unthemed
                // M3 light default regardless of dark mode - while the sheet's own text uses
                // DesignTheme.colors.content, which does flip to white in dark mode. That
                // mismatch made the content unreadable (white-on-light) in dark mode. Drive
                // the container off the same app theme the text uses.
                containerColor = DesignTheme.colors.surface
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
                onDismissRequest = { trustedNodeStep = TrustedNodeStep.None },
                // See the Paywall sheet above: same fix, same reason.
                containerColor = DesignTheme.colors.surface
            ) {
                SetTrustedNodeSheet(
                    currentHost = trustedNodeAddress,
                    currentPort = trustedNodePort,
                    onSubmit = { addressAndPort ->
                        onEvent.invoke(SettingsEvent.OnTrustedNodeAddressSubmitted(addressAndPort))
                        trustedNodeStep = TrustedNodeStep.None
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

private enum class TrustedNodeStep { None, Paywall, AddressEntry }
private fun formatTrustedNodeLabel(host: String?, port: String?): String? =
    host?.let { TrustedNode.withPort(it, port?.toIntOrNull() ?: 0) }

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
