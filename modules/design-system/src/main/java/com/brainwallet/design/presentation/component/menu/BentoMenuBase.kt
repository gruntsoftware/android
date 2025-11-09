package com.brainwallet.design.presentation.component.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.brainwallet.design.presentation.component.effect.MediumOpacityContainer
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

/**
 * Base component for bento-style menu items with consistent styling and layout.
 * Uses OpacityContainer for elegant glass effect appearance.
 */
@Composable
fun BentoMenuBase(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    onClick: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null
) {
    MediumOpacityContainer(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = BrainwalletTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = BrainwalletTheme.colors.content
                )
                description?.let {
                    Text(
                        text = it,
                        style = BrainwalletTheme.typography.bodySmall,
                        color = BrainwalletTheme.colors.content.copy(alpha = 0.7f)
                    )
                }
            }
            trailingContent?.invoke()
        }
    }
}
