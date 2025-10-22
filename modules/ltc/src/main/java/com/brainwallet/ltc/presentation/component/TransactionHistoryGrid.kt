package com.brainwallet.ltc.presentation.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.brainwallet.design.component.effect.CardGlassContainer
import com.brainwallet.design.R as DesignR
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

@Composable
fun TransactionHistoryGrid(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    CardGlassContainer(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = DesignR.drawable.ic_history),
                        contentDescription = "Transaction History",
                        tint = BrainwalletTheme.colors.content,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = "TRANSACTION HISTORY",
                        style = BrainwalletTheme.typography.titleMedium.copy(
                            color = BrainwalletTheme.colors.content,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "No transactions yet",
                    style = BrainwalletTheme.typography.bodyMedium.copy(
                        color = BrainwalletTheme.colors.content.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Normal
                    )
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
fun TransactionHistoryGridPreview() {
    BrainwalletTheme(isSystemInDarkTheme()) {
        TransactionHistoryGrid(
            modifier = Modifier.height(100.dp)
        )
    }
}
