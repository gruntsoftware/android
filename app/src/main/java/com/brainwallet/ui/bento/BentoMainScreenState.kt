package com.brainwallet.ui.bento

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.brainwallet.ltc.domain.model.TxItem
import com.brainwallet.ltc.presentation.navigation.LtcNavigation
import com.brainwallet.navigation.OnNavigate
import com.grunt.brainwallet.iap.presentation.model.ExportedTransaction
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun rememberBentoMainScreenState(onNavigate: OnNavigate): BentoMainScreenState {
    val context = LocalContext.current
    val ltcNavigation: LtcNavigation = koinInject {
        parametersOf(context, onNavigate)
    }
    return remember {
        BentoMainScreenState(
            ltcNavigation = ltcNavigation
        )
    }
}

@Stable
class BentoMainScreenState(
    initialRoute: String = "send",
    initialIsRailOpen: Boolean = false,
    initialShouldShowTransactionDetail: Boolean = false,
    initialShowExport: Boolean = false,
    initialSnappedTxItem: TxItem? = null,
    private val ltcNavigation: LtcNavigation? = null
) {

    enum class SheetType {
        DISMISSED, RECEIVE, EXPORT, SEND
    }

    var transactions by mutableStateOf(persistentListOf<TxItem>())
        private set

    var currentRoute by mutableStateOf(initialRoute)
        private set

    var isRailOpen by mutableStateOf(initialIsRailOpen)
        private set

    var shouldShowTransactionDetail by mutableStateOf(initialShouldShowTransactionDetail)
        private set

    var snappedTxItem by mutableStateOf(initialSnappedTxItem)
        private set

    var sheetType by mutableStateOf(SheetType.DISMISSED)
        private set

    val exportedTransaction get() = mapToExportedTransactions(transactions)

    fun onRouteChange(route: String) {
        currentRoute = route
        if (route == "buy_receive") {
            sheetType = SheetType.RECEIVE
        }
        if (route == "send") {
            sheetType = SheetType.SEND
        }
        if (route == "history") {
            shouldShowTransactionDetail = !shouldShowTransactionDetail
        }
        if (route == "game_hub") {
            ltcNavigation?.navigateToGames()
        }
    }

    fun toggleSheet(type: SheetType? = null) {
        if (type == null) {
            sheetType = SheetType.DISMISSED
            return
        }
        sheetType = if (sheetType == type) {
            SheetType.DISMISSED
        } else {
            type
        }
    }

    fun onTxSnapped(txItem: TxItem?) {
        if (txItem == null) return
        snappedTxItem = txItem
    }

    fun onTransactionChange(newTransactions: PersistentList<TxItem>) {
        transactions = newTransactions
    }

    fun toggleRail() {
        isRailOpen = !isRailOpen
    }

    private fun mapToExportedTransactions(txItems: List<TxItem>): PersistentList<ExportedTransaction> {
        return txItems.map {
            ExportedTransaction(
                timeStamp = it.timeStamp,
                blockHeight = it.blockHeight,
                txHashReversed = it.txReversed,
                sent = it.sent,
                received = it.received,
                fee = it.fee,
                to = it.to.toList()
            )
        }.toPersistentList()
    }
}
