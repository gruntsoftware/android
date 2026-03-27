package com.brainwallet.ui.composable

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.graphics.Color
import com.brainwallet.R
import com.brainwallet.navigation.Route
import com.brainwallet.constants.bentoBottomNavBarHt
import com.brainwallet.ui.theme.DesignTheme
import com.brainwallet.ui.theme.colorMidnite

/**
 * A custom bottom navigation bar component based on the provided design.
 *
 * @param modifier The modifier to be applied to the component.
 * @param currentRoute The currently selected navigation route.
 * @param onItemClick Callback invoked when a navigation item is clicked.
 */

@Composable
fun BentoBottomNavBar(
    isDarkMode: Boolean,
    isShowingTransactionDetail: Boolean,
    currentRoute: Route?,
    onItemClick: (Route) -> Unit,
    modifier: Modifier = Modifier,
) {
    var surfaceColor = if (isDarkMode) Color.Black else Color.White
    var contentsColor = if (isDarkMode) Color.White else colorMidnite

    NavigationBar(
        containerColor = surfaceColor,
        contentColor = contentsColor,
        modifier = modifier
            .navigationBarsPadding()
            .height(bentoBottomNavBarHt)
    ) {
        NavigationBarItem(
            selected = currentRoute is Route.Send,
            onClick = { onItemClick(Route.Send) },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_send),
                    contentDescription = stringResource(id = R.string.send_tab_description)
                )
            },
            label = { Text("Send") },
            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                selectedIconColor = contentsColor,
                selectedTextColor = contentsColor,
                indicatorColor = Color.Transparent,
                unselectedIconColor = contentsColor.copy(0.8f),
                unselectedTextColor = contentsColor.copy(0.8f)
            )
        )
        NavigationBarItem(
            selected = currentRoute is Route.BuyReceive,
            onClick = { onItemClick(Route.BuyReceive) },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_buy_receive),
                    contentDescription = stringResource(id = R.string.buy_receive_tab_description)
                )
            },
            label = { Text("Buy/Receive") },
            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                selectedIconColor = contentsColor,
                selectedTextColor = contentsColor,
                indicatorColor = Color.Transparent,
                unselectedIconColor = contentsColor.copy(0.8f),
                unselectedTextColor = contentsColor.copy(0.8f)
            )
        )
        NavigationBarItem(
            selected = currentRoute is Route.GameHub,
            onClick = { onItemClick(Route.GameHub) },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_game_hub),
                    contentDescription = stringResource(id = R.string.game_hub_tab_description)
                )
            },
            label = { Text("Game Hub") },
            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                selectedIconColor = contentsColor,
                selectedTextColor = contentsColor,
                indicatorColor = Color.Transparent,
                unselectedIconColor = contentsColor.copy(0.8f),
                unselectedTextColor = contentsColor.copy(0.8f)
            )
        )
        NavigationBarItem(
            selected = currentRoute is Route.History,
            onClick = { onItemClick(Route.History) },
            icon = {
                Icon(
                    painter = if (isShowingTransactionDetail) {
                        painterResource(R.drawable.home_24px)
                    } else {
                        painterResource(R.drawable.ic_history)
                    },
                    contentDescription = if (isShowingTransactionDetail) {
                        stringResource(id = R.string.home_tab_description)
                    } else {
                        stringResource(id = R.string.history_tab_description)
                    }
                )
            },
            label = {
                Text(
                    if (isShowingTransactionDetail) {
                        stringResource(R.string.home_icon_label)
                    } else {
                        stringResource(R.string.history_icon_label)
                    }
                )
            },
            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                selectedIconColor = contentsColor,
                selectedTextColor = contentsColor,
                indicatorColor = Color.Transparent,
                unselectedIconColor = contentsColor.copy(0.8f),
                unselectedTextColor = contentsColor.copy(0.8f)
            )
        )
    }
}

@Composable
@PreviewLightDark
fun BentoBottomNavBarPreview() {
    DesignTheme(isSystemInDarkTheme()) {
        BentoBottomNavBar(
            currentRoute = Route.Send,
            onItemClick = {},
            isDarkMode = isSystemInDarkTheme(),
            isShowingTransactionDetail = false
        )
    }
}
