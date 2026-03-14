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
import com.brainwallet.ui.layoutconstants.bottomNavHeight
import com.brainwallet.ui.theme.DesignTheme

/**
 * A custom bottom navigation bar component based on the provided design.
 *
 * @param modifier The modifier to be applied to the component.
 * @param currentRoute The currently selected navigation route.
 * @param onItemClick Callback invoked when a navigation item is clicked.
 */
@Composable
fun BentoBottomNavBar(
    currentRoute: Route?,
    onItemClick: (Route) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        containerColor = DesignTheme.colors.surface,
        contentColor = DesignTheme.colors.content,
        modifier = modifier
            .navigationBarsPadding()
            .height(bottomNavHeight)
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
                selectedIconColor = DesignTheme.colors.content,
                selectedTextColor = DesignTheme.colors.content,
                indicatorColor = Color.Transparent,
                unselectedIconColor = DesignTheme.colors.content.copy(0.8f),
                unselectedTextColor = DesignTheme.colors.content.copy(0.8f)
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
                selectedIconColor = DesignTheme.colors.content,
                selectedTextColor = DesignTheme.colors.content,
                indicatorColor = Color.Transparent,
                unselectedIconColor = DesignTheme.colors.content.copy(0.8f),
                unselectedTextColor = DesignTheme.colors.content.copy(0.8f)
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
                selectedIconColor = DesignTheme.colors.content,
                selectedTextColor = DesignTheme.colors.content,
                indicatorColor = Color.Transparent,
                unselectedIconColor = DesignTheme.colors.content.copy(0.8f),
                unselectedTextColor = DesignTheme.colors.content.copy(0.8f)
            )
        )
        NavigationBarItem(
            selected = currentRoute is Route.History,
            onClick = { onItemClick(Route.History) },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.ic_history),
                    contentDescription = stringResource(id = R.string.history_tab_description)
                )
            },
            label = { Text("History") },
            colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                selectedIconColor = DesignTheme.colors.content,
                selectedTextColor = DesignTheme.colors.content,
                indicatorColor = Color.Transparent,
                unselectedIconColor = DesignTheme.colors.content.copy(0.8f),
                unselectedTextColor = DesignTheme.colors.content.copy(0.8f)
            )
        )
    }
}

@Composable
@PreviewLightDark
fun BentoBottomNavBarPreview() {
    DesignTheme(isSystemInDarkTheme()) {
        BentoBottomNavBar(currentRoute = Route.Send, {})
    }
}
