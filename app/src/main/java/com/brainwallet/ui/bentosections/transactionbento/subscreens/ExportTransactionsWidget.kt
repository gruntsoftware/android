package com.brainwallet.ui.bentosections.transactionbento.subscreens
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.constants.transactionActionHt
import com.brainwallet.ui.bentosections.transactionbento.TransactionBentoViewModel
import com.brainwallet.ui.theme.IBMPlexSans
import com.brainwallet.ui.theme.bentoSurface
import org.koin.compose.viewmodel.koinViewModel
import com.brainwallet.R

@Composable
fun ExportedTransactionsWidget(
    isDarkMode: Boolean,
    modifier: Modifier = Modifier,
    viewModel: TransactionBentoViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .height(transactionActionHt)
            .bentoSurface(isDarkMode)
    ) {
        Row(
            modifier = Modifier
                .height(transactionActionHt)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.export_transaction_button_title),
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontFamily = IBMPlexSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = if (isDarkMode) Color.White else Color.Black
                ),
                maxLines = 1
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ExportedTransactionsWidgetPreview() {
    val isDarkMode = isSystemInDarkTheme()
    Box(modifier = Modifier) {
        ExportedTransactionsWidget(isDarkMode = isDarkMode)
    }
}
