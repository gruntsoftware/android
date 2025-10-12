package com.brainwallet.design.component.widget

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.brainwallet.design.R
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

/**
 * A widget that groups the Settings and Notifications action buttons,
 * styled according to the reference image.
 *
 * @param modifier The modifier to be applied to the component.
 * @param onSettingsClick The callback to be invoked when the settings button is clicked.
 * @param onNotificationsClick The callback to be invoked when the notifications button is clicked.
 */
@Composable
fun BentoTopBarActions(
    onSettingsClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = BrainwalletTheme.colors.surface),
        border = BorderStroke(1.dp, BrainwalletTheme.colors.content.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_gear),
                    contentDescription = "Settings",
                    tint = BrainwalletTheme.colors.content
                )
            }
            IconButton(onClick = onNotificationsClick) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_bell),
                    contentDescription = "Notifications",
                    tint = BrainwalletTheme.colors.content
                )
            }
        }
    }
}

@Composable
@PreviewLightDark
fun BentoTopBarActionsPreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        BentoTopBarActions(onSettingsClick = {}, onNotificationsClick = {})
    }
}
