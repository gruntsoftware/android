package com.brainwallet.ui.bentosections.transactionbento

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.brainwallet.ui.theme.IBMPlexSans

@Composable
fun TransactionDetail(
    dateTimestamp: String,
    amountString: String,
    ltcAddressString: String,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Text(
            modifier = Modifier,
            text = "Transaction Detail",
            style = TextStyle(
                fontFamily = IBMPlexSans,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                color = Color.White
            ),
            maxLines = 1
        )
    }
}
