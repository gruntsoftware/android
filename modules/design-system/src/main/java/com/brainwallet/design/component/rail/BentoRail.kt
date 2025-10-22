package com.brainwallet.design.component.rail

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

/**
 * Navigation rail component for the drawer content.
 * Contains user account, settings, learning banner, and app version information.
 */
@Composable
fun BentoRail(
    modifier: Modifier = Modifier,
    userName: String = "Joseph Sanjaya",
    appVersion: String = "v.X.X.X (XXXXXXXXXXXX)"
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        BentoRailAccount(userName, modifier = Modifier.weight(1f))
        BentoRailSettings(modifier = Modifier.weight(2f))
        BentoRailLearningBanner(modifier = Modifier.weight(0.5f))
        Spacer(Modifier.weight(0.25f))
        Text(
            text = "App version:\n$appVersion",
            style = BrainwalletTheme.typography.bodySmall,
            color = BrainwalletTheme.colors.content.copy(alpha = 0.7f),
            textAlign = TextAlign.Start,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
        )
    }
}

@PreviewLightDark
@Composable
fun BrainwalletRailPreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        BentoRail()
    }
}
