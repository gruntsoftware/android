package com.brainwallet.ltc.presentation.component.balance

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.brainwallet.design.presentation.component.effect.CardOpacityContainer
import com.brainwallet.ltc.R
import com.brainwallet.ltc.domain.model.BalanceState
import com.brainwallet.ltc.domain.model.SyncState
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme
import com.grunt.brainwallet.core.presentation.theme.blue
import com.grunt.brainwallet.core.presentation.theme.grape
import com.brainwallet.design.R as DesignR

@Composable
fun BalanceBentoGrid(
    modifier: Modifier = Modifier,
    uiState: BalanceBentoGridUiState = rememberBalanceBentoGridState(),
    onClick: () -> Unit = {},
    onSendClick: () -> Unit = {},
    onReceiveClick: () -> Unit = {}
) {
    val contentAlpha by animateFloatAsState(
        targetValue = if (uiState.isShown) 1f else 0.3f,
        animationSpec = tween(durationMillis = 300),
        label = "contentAlpha"
    )

    val onBalanceClick = {
        uiState.toggleCurrencyDisplay()
    }

    CardOpacityContainer(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            blue,
                            grape
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.wrapContentHeight()
            ) {
                HeaderSection(
                    isBalanceVisible = uiState.isShown,
                    onVisibilityToggle = { uiState.toggleShown() }
                )

                Spacer(modifier = Modifier.height(12.dp))

                BalanceSection(
                    balanceState = uiState.balanceState,
                    isBalanceVisible = uiState.isShown,
                    contentAlpha = contentAlpha,
                    showLtcPrimary = uiState.showLtcPrimary,
                    onBalanceClick = onBalanceClick
                )
            }
            BalanceBentoSyncDetails(
                uiState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 72.dp)
                    .align(Alignment.BottomCenter),
                onSendClick = onSendClick,
                onReceiveClick = onReceiveClick
            )
        }
    }
}

@Composable
private fun HeaderSection(
    isBalanceVisible: Boolean,
    onVisibilityToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.ltc_balance_header_title),
            style = BrainwalletTheme.typography.labelMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        )

        IconButton(
            onClick = onVisibilityToggle,
            modifier = Modifier.size(24.dp)
        ) {
            Icon(
                painter = painterResource(
                    id = if (isBalanceVisible) {
                        DesignR.drawable.ic_eye_enabled
                    } else {
                        DesignR.drawable.ic_eye_disabled
                    }
                ),
                contentDescription = if (isBalanceVisible) {
                    stringResource(R.string.ltc_balance_hide_content_description)
                } else {
                    stringResource(R.string.ltc_balance_show_content_description)
                },
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun BalanceSection(
    balanceState: BalanceState,
    isBalanceVisible: Boolean,
    contentAlpha: Float,
    showLtcPrimary: Boolean,
    onBalanceClick: () -> Unit
) {
    Column(
        modifier = Modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { onBalanceClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_ltc),
                    contentDescription = stringResource(R.string.ltc_balance_litecoin_content_description),
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            AnimatedContent(
                targetState = Pair(isBalanceVisible, showLtcPrimary),
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                        fadeOut(animationSpec = tween(300))
                },
                label = "balanceVisibility"
            ) { (visible, ltcPrimary) ->
                Text(
                    text = when {
                        !visible -> "•••••"
                        ltcPrimary -> balanceState.ltcValue
                        else -> balanceState.valueOnCurrency
                    },
                    style = BrainwalletTheme.typography.headlineLarge.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.alpha(contentAlpha)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        AnimatedContent(
            targetState = Pair(isBalanceVisible, showLtcPrimary),
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) togetherWith
                    fadeOut(animationSpec = tween(300))
            },
            label = "usdBalanceVisibility"
        ) { (visible, ltcPrimary) ->
            Text(
                text = when {
                    !visible -> "$ •••"
                    ltcPrimary -> balanceState.valueOnCurrency
                    else -> balanceState.ltcValue
                },
                style = BrainwalletTheme.typography.titleMedium.copy(
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier.alpha(contentAlpha)
            )
        }
    }
}

@Preview
@Composable
fun BalanceBentoGridSyncedPreview() {
    BrainwalletTheme(darkTheme = false) {
        BalanceBentoGrid(
            uiState = BalanceBentoGridUiState(
                initialSyncState = SyncState.Synced,
                initialBalance = BalanceState(),
                initialIsShown = true
            )
        )
    }
}

@Preview
@Composable
fun BalanceBentoGridSyncingPreview() {
    BrainwalletTheme(darkTheme = false) {
        BalanceBentoGrid(
            uiState = BalanceBentoGridUiState(
                initialSyncState = SyncState.Syncing(
                    progress = 0.8238,
                    timeStamp = "Dec 11, 2023 at 2:51PM",
                    currentBlockHeight = 257985534
                ),
                initialBalance = BalanceState(),
                initialIsShown = true
            )
        )
    }
}
