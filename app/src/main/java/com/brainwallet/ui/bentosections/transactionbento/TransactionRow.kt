package com.brainwallet.ui.bentosections.transactionbento
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.R
import com.brainwallet.constants.transactionRowHt
import com.brainwallet.presenter.entities.TxItem
import com.brainwallet.tools.util.BRExchange.ONE_LITECOIN_OF_LITOSHIS
import com.brainwallet.ui.composable.rememberWheelPickerState
import com.brainwallet.ui.theme.DesignTheme
import com.brainwallet.ui.theme.IBMPlexSans
import com.brainwallet.ui.theme.bentoDarkBorderGradient
import com.brainwallet.ui.theme.bentoDarkSurfaceGradient
import com.brainwallet.ui.theme.bentoLightBorderGradient
import com.brainwallet.ui.theme.bentoLightSurfaceGradient
import java.math.BigDecimal
import java.util.Date

@Composable
fun TransactionRow(
    txItem: TxItem,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier,
) {
    val gameHubBackground = R.drawable.game_hub_bk
    val wheelPickerState = rememberWheelPickerState(initialIndex = 0)
    var resizedLTCFiatFontSize by remember { mutableStateOf(44.sp) }
    var resizedAsOfFontSize by remember { mutableStateOf(12.sp) }
    var resizedLocalizedPriceFontSize by remember { mutableStateOf(20.sp) }
    val context = LocalContext.current

    val formatter = java.text.SimpleDateFormat(
        "MMM dd, yyyy hh:mm a",
        java.util.Locale.getDefault()
    )
    val dateTimestamp = formatter.format(Date(txItem.timeStamp * 1000L))
    val wasReceived = txItem.getSent() == 0L

    val ltcAddressString = if (wasReceived) {
        String.format("from ${txItem.from.firstOrNull()}" ?: "")
    } else {
        String.format("to ${txItem.to.firstOrNull()}" ?: "")
    }

    val amountReceived = BigDecimal(txItem.received).divide(BigDecimal(ONE_LITECOIN_OF_LITOSHIS))
    val amountSent = BigDecimal(txItem.received).divide(BigDecimal(ONE_LITECOIN_OF_LITOSHIS))
    val amountString = if (wasReceived) String.format("+Ł $amountReceived") else String.format("-Ł $amountSent")

    Box(
        modifier = Modifier
            .height(transactionRowHt)
            .background(
                brush = if (isDarkMode) bentoDarkSurfaceGradient else bentoLightSurfaceGradient,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 1.5.dp,
                brush = if (isDarkMode) bentoDarkBorderGradient else bentoLightBorderGradient,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.Start)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = dateTimestamp,
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = if (isDarkMode) Color.White else Color.Black
                    ),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = amountString,
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = if (wasReceived) DesignTheme.colors.affirm else DesignTheme.colors.error
                    ),
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    modifier = Modifier
                        .fillMaxWidth(0.5f),
                    text = ltcAddressString,
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.Light,
                        fontSize = 11.sp,
                        color = if (isDarkMode) {
                            Color.White.copy(0.5f)
                        } else {
                            Color.Black.copy(
                                0.5f
                            )
                        }
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.MiddleEllipsis
                )
            }
        }
    }
}
