package com.brainwallet.bento.component.rail
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import ltd.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

/**
 * A custom navigation rail component based on the provided design.
 * This view is meant to be used as the content for a navigation drawer.
 *
 * @param modifier The modifier to be applied to the component.
 * @param userName The name of the user to display.
 * @param appVersion The version of the app to display.
 */
@Composable
fun BentoRail(
    modifier: Modifier = Modifier,
    userName: String = "Joseph Sanjaya",
    appVersion: String = "v.X.X.X (XXXXXXXXXXXX)"
) {
    // ModalDrawerSheet provides the correct styling for content within a ModalNavigationDrawer
    ModalDrawerSheet(
        modifier = modifier
            .fillMaxHeight()
            .width(300.dp), // A standard width for a navigation drawer
        drawerContainerColor = BrainwalletTheme.colors.surface,
        drawerContentColor = BrainwalletTheme.colors.content
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            BentoRailAccount("Joseph Sanjaya", modifier = Modifier.weight(1f))
            BentoRailSettings(modifier = Modifier.weight(2f))
            BentoRailLearningBanner(modifier = Modifier.weight(0.5f))
            Spacer(Modifier.weight(0.25f))
            Text(
                text = "App version:\n$appVersion",
                style = BrainwalletTheme.typography.bodySmall,
                color = BrainwalletTheme.colors.content.copy(alpha = 0.7f),
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth().padding(
                    horizontal = 16.dp,
                    vertical = 24.dp
                )
            )
        }
    }
}

@Composable
@PreviewLightDark
fun BrainwalletRailPreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        BentoRail()
    }
}
