package com.brainwallet.ui.composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.R
import com.brainwallet.ui.theme.BrainwalletTheme
import com.brainwallet.ui.theme.lavender
import com.brainwallet.ui.theme.nearBlack

@Composable
fun MoonpayBuyButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier,
        shape = BrainwalletTheme.shapes.large,
        colors = ButtonDefaults.buttonColors(
            containerColor = lavender,
            contentColor = nearBlack
        ),
        enabled = enabled,
        onClick = onClick,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.buy_ltc).uppercase(),
                style = BrainwalletTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.powered_by_moonpay).uppercase(),
                    style = BrainwalletTheme.typography.labelSmall.copy(fontSize = 8.sp)
                )
                Image(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(R.drawable.ic_moonpay_logo),
                    contentDescription = "moonpay"
                )
            }
        }

    }

}