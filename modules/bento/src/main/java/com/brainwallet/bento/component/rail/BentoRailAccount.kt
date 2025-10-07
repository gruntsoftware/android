package com.brainwallet.bento.component.rail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.brainwallet.bento.R
import ltd.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

@Composable
fun BentoRailAccount(
    name: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = BrainwalletTheme.colors.background.copy(alpha = 0.2f)
        ),
        onClick = onClick
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 27.dp, horizontal = 19.dp)
        ) {
            Surface(
                shape = CircleShape,
                border = BorderStroke(1.dp, BrainwalletTheme.colors.content.copy(alpha = 0.2f)),
                color = BrainwalletTheme.colors.surface
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

@Composable
@PreviewLightDark
fun BentoRailAccountPreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        BentoRailAccount("Joseph Sanjaya")
    }
}
