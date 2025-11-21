package com.brainwallet.design.presentation.component.widget

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.brainwallet.design.R
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

private data class BentoBottomNavItem(
    val label: String,
    val icon: Int,
    val route: String
)

/**
 * Bottom navigation bar component with glass effect styling.
 * Provides main navigation options for the application.
 */
@Composable
fun BentoBottomNavBar(
    currentRoute: String?,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = listOf(
        BentoBottomNavItem(stringResource(R.string.design_bottom_nav_send), R.drawable.ic_sent, "send"),
        BentoBottomNavItem(stringResource(R.string.design_bottom_nav_buy_receive), R.drawable.ic_buy, "buy_receive"),
        BentoBottomNavItem(stringResource(R.string.design_bottom_nav_game_hub), R.drawable.ic_game_hub, "game_hub"),
        BentoBottomNavItem(stringResource(R.string.design_bottom_nav_history), R.drawable.ic_history, "history")
    )

    NavigationBar(
        modifier = modifier,
        containerColor = Color.Transparent,
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

@PreviewLightDark
@Composable
fun BentoBottomNavBarPreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        BentoBottomNavBar(currentRoute = "send", {})
    }
}
