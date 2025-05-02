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
    onClick: () -> Unit,
) {
    Button(
        modifier = modifier,
        shape = BrainwalletTheme.shapes.large,
        colors = ButtonDefaults.buttonColors(
            containerColor = lavender,
            contentColor = nearBlack
        ),
        onClick = onClick,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.ic_import),
                contentDescription = null
            )
            Text(
                text = stringResource(R.string.buy_ltc).uppercase(),
                style = BrainwalletTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.powered_by_moonpay).uppercase(),
                    style = BrainwalletTheme.typography.labelSmall.copy(fontSize = 8.sp)
                )
                Image(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(R.drawable.ic_moonpay_logo),
                    contentDescription = "moonpay"
                )
            }
        }

    }

}