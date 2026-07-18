package com.brainwallet.ui.bentosections.transactionbento.subscreens
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.R
import com.brainwallet.constants.BWConstants
import com.brainwallet.constants.bentoSpacer
import com.brainwallet.constants.transactionQRSize
import com.brainwallet.presenter.entities.ServiceItems
import com.brainwallet.presenter.entities.TxItem
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.qrcode.QRUtils
import com.brainwallet.tools.util.BRCurrency
import com.brainwallet.tools.util.BRExchange
import com.brainwallet.tools.util.BRExchange.ONE_LITECOIN_OF_LITOSHIS
import com.brainwallet.tools.util.Utils
import com.brainwallet.ui.theme.IBMPlexSans
import com.brainwallet.ui.theme.mainBentoSurface
import timber.log.Timber
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.collections.toHashSet
import androidx.compose.ui.platform.LocalLocale

@Composable
fun TransactionDetail(
    isDarkMode: Boolean,
    currentTransaction: TxItem?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    // LTC address to recipient or Brainwallet user
    val ltcAddress: String?

    // determine sent vs received state
    val wasSentVsReceived = ((currentTransaction?.received ?: 0L) - (currentTransaction?.sent ?: 0L)) < 0

    // Output addresses
    val outputAddressSet = currentTransaction?.to?.toHashSet()
    val opsSet = Utils.fetchServiceItem(context, ServiceItems.OPSALL)
        .split(",")
        .toHashSet()

    ltcAddress = outputAddressSet
        ?.filter { it !in opsSet }
        ?.filterNotNull()
        ?.firstOrNull() ?: "ERROR-ADDRESS"

    val toFromAddressFormatting: String = if (wasSentVsReceived) {
        String.format(stringResource(R.string.TransactionDetails_to), ltcAddress)
    } else {
        String.format(stringResource(R.string.TransactionDetails_from), ltcAddress)
    }

    val formatter = SimpleDateFormat(
        "MMM dd, yyyy hh:mm a",
        LocalLocale.current.platformLocale
    )
    val dateTimestamp = formatter.format(Date(currentTransaction?.timeStamp?.times(1000L) ?: 0L))
    val wasReceived = currentTransaction?.getSent() == 0L
    val amountReceived =
        BigDecimal(currentTransaction?.received ?: 0L).divide(
            BigDecimal(ONE_LITECOIN_OF_LITOSHIS),
            8,
            BWConstants.ROUNDING_MODE
        )

    val outAmounts: LongArray? = currentTransaction?.getOutAmounts()
    var opsAmount = Long.Companion.MAX_VALUE
    if (outAmounts?.size == 3) {
        for (i in outAmounts.indices) {
            val value = outAmounts[i]

            if (value < opsAmount && value != 0L) {
                opsAmount = value
                Timber.d("timber: outAmounts size %d opsAmount value: %d", outAmounts.size, value)
            }
        }
    } else {
        opsAmount = 0L
    }

    val txWasReceived = currentTransaction?.getReceived() ?: 0L
    val txWasSent = currentTransaction?.getSent() ?: 0L

    val sentLitoshisAmount: Long = when {
        wasReceived -> txWasReceived
        else -> txWasSent - txWasReceived - opsAmount
    }
    val isLTCPreferred = BRSharedPrefs.getLTCViewingPreference(context)
    val iso = if (isLTCPreferred) "LTC" else BRSharedPrefs.getIsoSymbol(context)

    val formattedAmount = BRCurrency.getFormattedCurrencyString(
        context,
        iso,
        BRExchange.getAmountFromLitoshis(context, iso, BigDecimal(sentLitoshisAmount))
    )

    val amountSent =
        BigDecimal(currentTransaction?.sent ?: 0L).divide(
            BigDecimal(ONE_LITECOIN_OF_LITOSHIS),
            8,
            BWConstants.ROUNDING_MODE
        )
    val combinedFees = BigDecimal(currentTransaction?.fee ?: 0L) + BigDecimal(opsAmount)
    val feesLitoshis = combinedFees.divide(BigDecimal(ONE_LITECOIN_OF_LITOSHIS))
    val feesTotal = String.format("-Ł $feesLitoshis")
    val qrBitmap = QRUtils.generateQR(context, "litecoin:$ltcAddress").asImageBitmap()
    val txIDBrowserURL: String = run {
        "${BWConstants.BLOCKCHAIR_EXPLORER_BASE_URL}${currentTransaction?.txHashHexReversed ?: ""}"
    }
    val sectionHeight = 220.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(sectionHeight)
            .mainBentoSurface(isDarkMode)
    ) {
        Column {
            Row(
                modifier = Modifier.padding(bentoSpacer)
            ) {
                Column(
                    modifier = Modifier
                        .padding(all = 6.dp)
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
                    if (!wasReceived) {
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
                    }
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
                        .padding(all = 6.dp)
                ) {
                    Text(
                        modifier = Modifier,
                        text = formattedAmount,
                        style = TextStyle(
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (isDarkMode) Color.White else Color.Black
                        ),
                        maxLines = 1
                    )
                    if (!wasReceived) {
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
                    }
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
                        maxLines = 3
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
                    .fillMaxWidth()
                    .padding(bentoSpacer)
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
                        .fillMaxWidth(0.5f)
                        .padding(4.dp),
                    text = ltcAddress,
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = if (isDarkMode) Color.White else Color.Black
                    ),
                    maxLines = 3
                )
            }
        }
    }
}
