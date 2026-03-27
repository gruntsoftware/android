package com.brainwallet.ui.bentosections.transactionbento

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.brainwallet.ui.theme.bentoDarkBorderGradient
import com.brainwallet.ui.theme.bentoDarkSurfaceGradient
import com.brainwallet.ui.theme.bentoLightBorderGradient
import com.brainwallet.ui.theme.bentoLightSurfaceGradient
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TransactionsBentoScreen(
    showTransactionDetail: Boolean,
    modifier: Modifier = Modifier,
    viewModel: TransactionBentoViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()
    TransactionsBentoScreen(
        state = state,
        showTransactionDetail = showTransactionDetail,
        modifier = modifier
    )
}

@Composable
fun TransactionsBentoScreen(
    state: TransactionBentoState,
    showTransactionDetail: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = if (state.darkMode) bentoDarkSurfaceGradient else bentoLightSurfaceGradient,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.5.dp,
                brush = if (state.darkMode) bentoDarkBorderGradient else bentoLightBorderGradient,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
    }
}
