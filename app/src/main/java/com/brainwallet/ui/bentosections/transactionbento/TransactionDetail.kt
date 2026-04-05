package com.brainwallet.ui.bentosections.transactionbento
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.Image
import com.brainwallet.R
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.constants.transactionQRSize
import com.brainwallet.presenter.entities.TxItem
import com.brainwallet.tools.qrcode.QRUtils
import com.brainwallet.tools.util.BRExchange.ONE_LITECOIN_OF_LITOSHIS
import com.brainwallet.ui.theme.IBMPlexSans
import com.brainwallet.ui.theme.bentoDarkBorderGradient
import com.brainwallet.ui.theme.bentoDarkSurfaceGradient
import com.brainwallet.ui.theme.bentoLightBorderGradient
import com.brainwallet.ui.theme.bentoLightSurfaceGradient
import timber.log.Timber
import java.math.BigDecimal
import java.util.Date
@Composable
fun TransactionDetail(
    isDarkMode: Boolean,
    currentTransaction: TxItem?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val ltcAddressString = currentTransaction?.to?.firstOrNull() ?: ""
    val formatter = java.text.SimpleDateFormat(
        "MMM dd, yyyy hh:mm a",
        java.util.Locale.getDefault()
    )
    val dateTimestamp = formatter.format(Date(currentTransaction?.timeStamp?.times(1000L) ?: 0L))
    val wasReceived = currentTransaction?.getSent() == 0L
    val amountReceived =
        BigDecimal(currentTransaction?.received ?: 0L).divide(BigDecimal(ONE_LITECOIN_OF_LITOSHIS))
    val amountSent =
        BigDecimal(currentTransaction?.sent ?: 0L).divide(BigDecimal(ONE_LITECOIN_OF_LITOSHIS))
    val feesLitoshis = BigDecimal(currentTransaction?.fee ?: 0L).divide(BigDecimal(ONE_LITECOIN_OF_LITOSHIS))
    val feesTotal = String.format("-Ł $feesLitoshis")
    val amountString =
        if (wasReceived) String.format("+Ł $amountReceived") else String.format("-Ł $amountSent")
    val qrBitmap = QRUtils.generateQR(context, "litecoin:$ltcAddressString").asImageBitmap()

    Box(
        modifier = modifier
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
        Column {
            Row(
                modifier = Modifier.padding(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(end = 8.dp)
                ) {
                    Text(
                        modifier = Modifier,
                        text = stringResource(R.string.amount_label),
                        style = TextStyle(
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (isDarkMode) Color.White else Color.Black
                        ),
                        maxLines = 1
                    )
                    Text(
                        modifier = Modifier,
                        text = stringResource(R.string.fees_label),
                        style = TextStyle(
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (isDarkMode) Color.White else Color.Black
                        ),
                        maxLines = 1
                    )
                    Text(
                        modifier = Modifier,
                        text = stringResource(R.string.tx_id_label),
                        style = TextStyle(
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (isDarkMode) Color.White else Color.Black
                        ),
                        maxLines = 1
                    )
                    Text(
                        modifier = Modifier,
                        text = stringResource(R.string.memo_label),
                        style = TextStyle(
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (isDarkMode) Color.White else Color.Black
                        ),
                        maxLines = 1
                    )
                    Text(
                        modifier = Modifier,
                        text = stringResource(R.string.block_height_label),
                        style = TextStyle(
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (isDarkMode) Color.White else Color.Black
                        ),
                        maxLines = 1
                    )
                    Text(
                        modifier = Modifier,
                        text = stringResource(R.string.date_label),
                        style = TextStyle(
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (isDarkMode) Color.White else Color.Black
                        ),
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.weight(0.2f))
                Column(
                    modifier = Modifier
                ) {
                    Text(
                        modifier = Modifier,
                        text = amountString,
                        style = TextStyle(
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (isDarkMode) Color.White else Color.Black
                        ),
                        maxLines = 1
                    )
                    Text(
                        modifier = Modifier,
                        text = "$feesTotal",
                        style = TextStyle(
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (isDarkMode) Color.White else Color.Black
                        ),
                        maxLines = 1
                    )
                    Text(
                        modifier = Modifier,
                        text = currentTransaction?.txHashHexReversed ?: "",
                        style = TextStyle(
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (isDarkMode) Color.White else Color.Black
                        ),
                        maxLines = 1
                    )
                    Text(
                        modifier = Modifier,
                        text = "${currentTransaction?.metaData ?: ""}",
                        style = TextStyle(
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (isDarkMode) Color.White else Color.Black
                        ),
                        maxLines = 1
                    )
                    Text(
                        modifier = Modifier,
                        text = "${currentTransaction?.blockHeight ?: 0L}",
                        style = TextStyle(
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (isDarkMode) Color.White else Color.Black
                        ),
                        maxLines = 1
                    )
                    Text(
                        modifier = Modifier,
                        text = "$dateTimestamp",
                        style = TextStyle(
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (isDarkMode) Color.White else Color.Black
                        ),
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
            }
            Row(
                modifier = Modifier
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Image(
                        modifier = Modifier
                            .height(transactionQRSize)
                            .width(transactionQRSize),
                        bitmap = qrBitmap,
                        contentDescription = "qr address",
                        colorFilter = ColorFilter.tint(if (isDarkMode) Color.White else Color.Black)
                    )

                    Text(
                        modifier = Modifier
                            .padding(6.dp),
                        text = ltcAddressString,
                        style = TextStyle(
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = if (isDarkMode) Color.White else Color.Black
                        ),
                        maxLines = 1
                    )

                    Text(
                        modifier = Modifier
                            .padding(6.dp)
                            .testTag("EXPORT_IAP_PLACEHOLDER"),
                        text = "",
                        style = TextStyle(
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.Normal,
                            fontSize = 1.sp,
                            color = if (isDarkMode) Color.White else Color.Black
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}
