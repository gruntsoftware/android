package com.brainwallet.ltc.presentation.component.balance

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.brainwallet.ltc.R
import com.brainwallet.ltc.domain.model.SyncState
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme
import com.grunt.brainwallet.core.presentation.theme.chili
import com.grunt.brainwallet.core.presentation.theme.pesto

@Composable
fun BalanceBentoSyncDetails(
    uiState: BalanceBentoGridUiState,
    modifier: Modifier = Modifier,
    onSendClick: () -> Unit = {},
    onReceiveClick: () -> Unit = {}
) {
    val syncState = uiState.syncState
    val syncingState = syncState as? SyncState.Syncing

    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        AnimatedVisibility(syncingState != null) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Syncing...",
                    modifier = Modifier.fillMaxWidth(),
                    style = BrainwalletTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.End
                    )
                )
                Text(
                    "Last block: ${uiState.lastBlock}",
                    modifier = Modifier.fillMaxWidth(),
                    style = BrainwalletTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.End
                    )
                )
                Text(
                    "Date: ${syncingState?.timeStamp}",
                    modifier = Modifier.fillMaxWidth(),
                    style = BrainwalletTheme.typography.bodyMedium.copy(
                        color = Color.White.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.End
                    )
                )
            }
        }
        Row {
            AnimatedVisibility(syncingState != null) {
                Row(
                    modifier = Modifier.wrapContentWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${((syncingState?.progress ?: 0.0) * 100).toInt()}%",
                        style = BrainwalletTheme.typography.titleLarge.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    if ((syncingState?.currentBlockHeight ?: 0) > 0) {
                        Text(
                            text = "Block: ${syncingState?.currentBlockHeight}",
                            style = BrainwalletTheme.typography.bodyMedium.copy(
                                color = Color.White.copy(alpha = 0.6f)
                            )
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            ActionButtonsSection(onSendClick, onReceiveClick)
        }
        AnimatedVisibility(syncingState != null) {
            LinearProgressIndicator(
                progress = { (syncingState?.progress ?: 0f).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFF4CAF50),
                trackColor = Color.White.copy(alpha = 0.2f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun ActionButtonsSection(
    onSendClick: () -> Unit,
    onReceiveClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.wrapContentWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ActionButton(
            text = "Send",
            iconRes = R.drawable.ic_send,
            iconTint = chili,
            onClick = onSendClick
        )

        VerticalDivider()

        ActionButton(
            text = "Receive",
            iconRes = R.drawable.ic_receive,
            iconTint = pesto,
            onClick = onReceiveClick
        )
    }
}

@Composable
private fun ActionButton(
    text: String,
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = Color.White,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier.clickable { onClick() }
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = text,
            modifier = Modifier.size(16.dp),
            tint = iconTint
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            style = BrainwalletTheme.typography.labelLarge.copy(
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}
