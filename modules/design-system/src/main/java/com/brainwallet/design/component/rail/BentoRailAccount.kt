package com.brainwallet.design.component.rail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.brainwallet.design.R
import com.brainwallet.design.component.effect.MediumGlassContainer
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

/**
 * Account section component for the navigation rail.
 * Displays user information with glass effect styling.
 */
@Composable
fun BentoRailAccount(
    name: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    MediumGlassContainer(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 27.dp, horizontal = 19.dp)
        ) {
            Surface(
                shape = CircleShape,
                border = BorderStroke(1.dp, BrainwalletTheme.colors.content.copy(alpha = 0.2f)),
                color = BrainwalletTheme.colors.surface.copy(alpha = 0.3f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(10.25.dp)
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_user_circle),
                        contentDescription = "User logo",
                        colorFilter = ColorFilter.tint(BrainwalletTheme.colors.content),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        name,
                        style = BrainwalletTheme.typography.bodySmall.copy(
                            color = BrainwalletTheme.colors.content,
                            textAlign = TextAlign.Start
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Account Information",
                    style = BrainwalletTheme.typography.bodyLarge.copy(
                        color = BrainwalletTheme.colors.content,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
fun BentoRailAccountPreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        BentoRailAccount("Joseph Sanjaya")
    }
}
