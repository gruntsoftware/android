package com.brainwallet.ui.bentosections.transactionbento.subscreens
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.ui.bentosections.transactionbento.TransactionBentoViewModel
import com.brainwallet.ui.theme.IBMPlexSans
import com.brainwallet.ui.theme.bentoSurface
import org.koin.compose.viewmodel.koinViewModel
import com.brainwallet.R
import com.brainwallet.presenter.entities.TxItem
import com.brainwallet.ui.bentosections.transactionbento.TransactionFilterState
import com.brainwallet.ui.theme.DesignTheme
import kotlinx.collections.immutable.ImmutableList

@Composable
fun TransactionFilterWidget(
    isDarkMode: Boolean,
    toggleState: TransactionFilterState,
    toggleStateIcon: Painter,
    transactions: ImmutableList<TxItem>,
    modifier: Modifier = Modifier,
    viewModel: TransactionBentoViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    val toggleStateIconTint = if (toggleState == TransactionFilterState.ALL) {
        ColorFilter.tint(if (state.darkMode) Color.White else DesignTheme.colors.affirm)
    } else if (toggleState == TransactionFilterState.RECEIVED) {
        ColorFilter.tint(DesignTheme.colors.affirm)
    } else {
        ColorFilter.tint(DesignTheme.colors.error)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .bentoSurface(isDarkMode)
    ) {
        Row(
            modifier = Modifier.fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                modifier = Modifier
                    .height(30.dp)
                    .height(30.dp)
                    .padding(top = 6.dp, bottom = 6.dp, end = 10.dp)
                    .padding(start = 12.dp),
                painter = toggleStateIcon,
                contentDescription = "circle_circle_icon",
                contentScale = ContentScale.Fit,
                colorFilter = toggleStateIconTint,
                alignment = Alignment.CenterStart
            )
            Text(
                modifier = Modifier
                    .padding(6.dp),
                text = stringResource(toggleState.labelRes).uppercase(),
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontFamily = IBMPlexSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                ),
                maxLines = 1,
                color = if (state.darkMode) Color.White else DesignTheme.colors.affirm
            )
            Spacer(modifier = Modifier.weight(1f))

            Text(
                modifier = Modifier.padding(top = 6.dp, bottom = 6.dp, end = 12.dp),
                text = "${transactions.size}  " +
                    stringResource(com.brainwallet.R.string.txns_label),
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontFamily = IBMPlexSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                ),
                maxLines = 1,
                color = if (state.darkMode) Color.White else DesignTheme.colors.affirm
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun TransactionFilterWidgetPreview() {
    val isDarkMode = isSystemInDarkTheme()
    Box(modifier = Modifier) {
    }
}
