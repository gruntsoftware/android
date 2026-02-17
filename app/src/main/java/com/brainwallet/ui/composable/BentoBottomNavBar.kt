package com.brainwallet.ui.composable

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.brainwallet.design.R
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

/**
 * Data class representing an item in the bottom navigation bar.
 * @param label The text label for the item.
 * @param icon The icon for the item.
 * @param route The navigation route associated with the item.
 */
private data class BentoBottomNavItem(
    val label: String,
    val icon: Int,
    val route: String
)

/**
 * A custom bottom navigation bar component based on the provided design.
 *
 * @param modifier The modifier to be applied to the component.
 * @param currentRoute The currently selected navigation route.
 * @param onItemClick Callback invoked when a navigation item is clicked.
 */
@Composable
fun BentoBottomNavBar(
    currentRoute: String?,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = remember {
        listOf(
            BentoBottomNavItem("Send", R.drawable.ic_sent, "send"),
            BentoBottomNavItem("Buy/Receive", R.drawable.ic_buy, "buy_receive"),
            BentoBottomNavItem("Game Hub", R.drawable.ic_game_hub, "game_hub"),
            BentoBottomNavItem("History", R.drawable.ic_history, "history")
        )
    }

    NavigationBar(
        modifier = modifier,
        containerColor = BrainwalletTheme.colors.surface,
        contentColor = BrainwalletTheme.colors.content
    ) {
        items.forEach { item ->
            val isSelected = item.route == currentRoute
            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemClick(item.route) },
                icon = {
                    Icon(
                        painter = painterResource(item.icon),
                        contentDescription = item.label
                    )
                },
                label = { Text(text = item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BrainwalletTheme.colors.content,
                    selectedTextColor = BrainwalletTheme.colors.content,
                    unselectedIconColor = BrainwalletTheme.colors.content.copy(alpha = 0.6f),
                    unselectedTextColor = BrainwalletTheme.colors.content.copy(alpha = 0.6f),
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}

@Composable
@PreviewLightDark
fun BentoBottomNavBarPreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        BentoBottomNavBar(currentRoute = "send", {})
    }
}
