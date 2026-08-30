package com.brainwallet.ui.screens.settings.settingsrows

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.brainwallet.R
import com.brainwallet.tools.util.TrustedNode
import com.brainwallet.ui.theme.DesignTheme

/**
 * Second step of the trusted-LTC-node flow, shown once the feature has been purchased
 * (see [LitecoinBlockchainDetail]). Collects the IPv4 address and optional port of the
 * node `BRPeerManager` should sync against, and only emits a value that passes
 * [TrustedNode.isValid] via [onSubmit]. Persistence + peer reconnect happen in the
 * ViewModel.
 */
@Composable
fun SetTrustedNodeSheet(
    currentAddress: String?,
    onSubmit: (address: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var host by remember {
        mutableStateOf(currentAddress?.let(TrustedNode::getNodeHost).orEmpty())
    }
    var port by remember {
        mutableStateOf(
            currentAddress
                ?.let(TrustedNode::getNodePort)
                ?.takeIf { it > 0 }
                ?.toString()
                .orEmpty()
        )
    }

    val combined = if (port.isBlank()) host.trim() else "${host.trim()}:${port.trim()}"
    val isValid = TrustedNode.isValid(combined)
    val showError = combined.isNotBlank() && !isValid

    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.trusted_node_entry_title),
            style = DesignTheme.typography.titleMedium,
            color = DesignTheme.colors.content,
        )
        Text(
            text = stringResource(R.string.trusted_node_entry_description),
            style = DesignTheme.typography.bodyMedium,
            color = DesignTheme.colors.content,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                modifier = Modifier.weight(2f),
                value = host,
                onValueChange = { host = it.trim() },
                singleLine = true,
                isError = showError,
                label = { Text(stringResource(R.string.trusted_node_entry_ip_label)) },
                placeholder = { Text("192.168.1.10") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            )
            OutlinedTextField(
                modifier = Modifier.weight(1f),
                value = port,
                onValueChange = { new -> port = new.filter(Char::isDigit).take(5) },
                singleLine = true,
                isError = showError,
                label = { Text(stringResource(R.string.trusted_node_entry_port_label)) },
                placeholder = { Text("9333") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        }

        if (showError) {
            Text(
                text = stringResource(R.string.trusted_node_entry_error),
                style = DesignTheme.typography.bodySmall,
                color = DesignTheme.colors.error,
            )
        }

        Button(
            onClick = { onSubmit(combined) },
            enabled = isValid,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.trusted_node_entry_save))
        }
    }
}
