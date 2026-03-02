package com.brainwallet.ui.composable

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.brainwallet.R
import com.brainwallet.navigation.Route
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

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
        modifier = modifier,
        containerColor = BrainwalletTheme.colors.surface,
        contentColor = BrainwalletTheme.colors.content
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
            label = { Text("Send") }
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
            label = { Text("Buy/Receive") }
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
            label = { Text("Game Hub") }
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
            label = { Text("History") }
        )
    }
}

@Composable
@PreviewLightDark
fun BentoBottomNavBarPreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        BentoBottomNavBar(currentRoute = Route.Send, {})
    }
}
