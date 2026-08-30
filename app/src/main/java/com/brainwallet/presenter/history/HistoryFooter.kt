package com.brainwallet.presenter.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.R
import com.brainwallet.data.model.AppSetting
import com.brainwallet.ui.composable.BorderedLargeButton
import com.brainwallet.ui.theme.BrainwalletAppTheme
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import com.brainwallet.ui.theme.DesignTheme
import com.brainwallet.ui.theme.LocalIsDarkModeFlag
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme
import com.grunt.brainwallet.core.presentation.util.BaseViewModel
import com.grunt.brainwallet.iap.export.transactions.presentation.model.ExportedTransaction
import com.grunt.brainwallet.iap.export.transactions.presentation.screen.ExportTrxSheet
import org.koin.android.annotation.KoinViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryFooter(
    exportedTransactions: PersistentList<ExportedTransaction>,
    modifier: Modifier = Modifier,
    viewModel: HistoryFooterViewModel = koinViewModel()
) {
    val showSheet by viewModel.collectAsState()
    AnimatedVisibility(
        exportedTransactions.isNotEmpty(),
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(vertical = 16.dp)
    ) {
        HistoryFooter(
            showSheet = showSheet,
            exportedTransactions = exportedTransactions,
            onClick = viewModel::onClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryFooter(
    showSheet: Boolean,
    exportedTransactions: PersistentList<ExportedTransaction>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    Box(
        modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        BorderedLargeButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
        ) {
            Text(
                text = stringResource(R.string.cta_export_transaction),
                fontSize = 16.sp,
                fontWeight = FontWeight.Thin,
            )
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            sheetState = sheetState,
            containerColor = DesignTheme.colors.surface,
            contentColor = DesignTheme.colors.content,
            onDismissRequest = onClick
        ) {
            // ExportTrxSheet reads colors/typography from the core module's own
            // BrainwalletTheme composition locals, which are only populated inside
            // this wrapper - without it those resolve to Color.Unspecified.
            BrainwalletTheme(darkTheme = LocalIsDarkModeFlag.current) {
                ExportTrxSheet(
                    transactions = exportedTransactions,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
@PreviewLightDark
fun HistoryFooterPreview() {
    BrainwalletAppTheme(AppSetting(isDarkMode = isSystemInDarkTheme())) {
        HistoryFooter(exportedTransactions = persistentListOf(), showSheet = false)
    }
}

@KoinViewModel
class HistoryFooterViewModel : BaseViewModel<Boolean, Unit>(false) {
    fun onClick() = intent { reduce { !state } }
}
