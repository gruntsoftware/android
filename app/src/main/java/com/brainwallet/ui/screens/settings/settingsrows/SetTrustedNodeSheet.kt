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
import androidx.compose.material3.OutlinedTextFieldDefaults
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
 * (see [LitecoinBlockchainDetail]). Collects the IPv4 address and port of the node
 * `BRPeerManager` should sync against, and only emits a value that passes
 * [TrustedNode.isValid] via [onSubmit]. Persistence (as independent BRKeyStore values -
 * see `SettingsViewModel.OnTrustedNodeAddressSubmitted`) + peer reconnect happen in the
 * ViewModel.
 *
 * The port is never left implicit: it's pre-filled with [TrustedNode.STANDARD_PORT] when
 * absent (a fresh entry, or [currentPort] not yet set) and always folded into the submitted
 * [addressAndPort] string via [TrustedNode.withPort], so what's saved - and what the
 * settings row's button label shows - always states the exact port a connection is made
 * on, rather than relying on BRPeerManager's native fallback silently.
 */
@Composable
fun SetTrustedNodeSheet(
    currentHost: String?,
    currentPort: String?,
    onSubmit: (addressAndPort: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var host by remember { mutableStateOf(currentHost.orEmpty()) }
    var port by remember {
        mutableStateOf(
            currentPort
                ?.toIntOrNull()
                ?.takeIf { it > 0 }
                ?.toString()
                ?: TrustedNode.STANDARD_PORT.toString()
        )
    }

    val trimmedHost = host.trim()
    // Only fold in the port once there's a host to attach it to - otherwise a blank sheet
    // would combine to ":9333", which fails validation and would show an error before the
    // user has typed anything.
    val combined = if (trimmedHost.isBlank()) {
        ""
    } else {
        TrustedNode.withPort(trimmedHost, port.trim().toIntOrNull() ?: 0)
    }
    val isValid = TrustedNode.isValid(combined)
    val showError = combined.isNotBlank() && !isValid

    // OutlinedTextField's default colors come from MaterialTheme.colorScheme, which
    // DesignTheme never themes (only typography) - so its onSurface-derived text/label/
    // outline stayed the unthemed light default (near-black) regardless of dark mode. Once
    // the enclosing sheet's background is correctly dark (see LitecoinBlockchainDetail's
    // ModalBottomSheet), that near-black text on a dark surface is unreadable. Drive it off
    // the same DesignTheme tokens the rest of this sheet already uses.
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = DesignTheme.colors.content,
        unfocusedTextColor = DesignTheme.colors.content,
        focusedLabelColor = DesignTheme.colors.content,
        unfocusedLabelColor = DesignTheme.colors.content.copy(alpha = 0.6f),
        focusedPlaceholderColor = DesignTheme.colors.content.copy(alpha = 0.4f),
        unfocusedPlaceholderColor = DesignTheme.colors.content.copy(alpha = 0.4f),
        focusedBorderColor = DesignTheme.colors.content,
        unfocusedBorderColor = DesignTheme.colors.border,
        cursorColor = DesignTheme.colors.content,
        errorTextColor = DesignTheme.colors.error,
        errorLabelColor = DesignTheme.colors.error,
        errorBorderColor = DesignTheme.colors.error,
        errorCursorColor = DesignTheme.colors.error,
    )

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
                colors = textFieldColors,
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
                colors = textFieldColors,
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
