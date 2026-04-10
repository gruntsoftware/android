package com.brainwallet.ui.screens.send
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import org.koin.compose.viewmodel.koinViewModel
import com.brainwallet.R

@Composable
fun PreSend(
    modifier: Modifier = Modifier,
    viewModel: SendViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .background(if (state.darkMode) Color.Transparent else Color.White),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PreSendAddressRow(
                label = stringResource(R.string.recipient_label),
                value = state.recipientLTCAddress
            )
            PreSendAmountRow(
                label = stringResource(R.string.amount_label),
                value = state.amountString
            )
            PreSendMemoRow(
                label = stringResource(R.string.memo_label),
                value = state.userMemorandum
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
