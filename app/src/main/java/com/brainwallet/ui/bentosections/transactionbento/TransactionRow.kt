package com.brainwallet.ui.bentosections.transactionbento
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.constants.transactionRowHt
import com.brainwallet.ui.composable.SmallToggleButton
import com.brainwallet.ui.theme.IBMPlexSans

@Composable
fun TransactionRow(
    dateTimestamp: String,
    amountString: String,
    ltcAddressString: String,
    filterState: TransactionFilterState,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(transactionRowHt),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.Start)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = dateTimestamp,
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = Color.White
                    ),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = amountString,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SmallToggleButton(
                onClick = {},
                enabled = filterState == TransactionFilterState.ALL
            ) {
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = ltcAddressString,
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
}
