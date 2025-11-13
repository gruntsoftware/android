package com.brainwallet.ui.bento

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.brainwallet.ltc.presentation.navigation.LtcNavigation
import com.brainwallet.navigation.OnNavigate
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun rememberBentoMainScreenState(onNavigate: OnNavigate): BentoMainScreenState {
    val ltcNavigation: LtcNavigation = koinInject { parametersOf(onNavigate) }
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
    initialShouldShowReceiveDialog: Boolean = false,
    private val ltcNavigation: LtcNavigation? = null
) {

    enum class SheetType {
        DISMISSED, RECEIVE, SEND
    }

    var currentRoute by mutableStateOf(initialRoute)
        private set

    var isRailOpen by mutableStateOf(initialIsRailOpen)
        private set

    var sheetType by mutableStateOf(SheetType.DISMISSED)
        private set

    fun onRouteChange(route: String) {
        currentRoute = route
        if (route == "buy_receive") {
            sheetType = SheetType.RECEIVE
        }
        if (route == "send") {
            sheetType = SheetType.SEND
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

    fun toggleRail() {
        isRailOpen = !isRailOpen
    }
}
