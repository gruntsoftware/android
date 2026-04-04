package com.brainwallet.ui.bentosections.transactionbento
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.constants.BWConstants
import com.brainwallet.constants.transactionRowHt
import com.brainwallet.data.model.LtcStats
import com.brainwallet.presenter.entities.ServiceItems
import com.brainwallet.presenter.entities.TxItem
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.util.BRExchange.ONE_LITECOIN_OF_LITOSHIS
import com.brainwallet.tools.util.Utils
import com.brainwallet.ui.composable.rememberWheelPickerState
import com.brainwallet.ui.theme.DesignTheme
import com.brainwallet.ui.theme.IBMPlexSans
import com.brainwallet.ui.theme.bentoDarkBorderGradient
import com.brainwallet.ui.theme.bentoDarkSurfaceGradient
import com.brainwallet.ui.theme.bentoLightBorderGradient
import com.brainwallet.ui.theme.bentoLightSurfaceGradient
import com.brainwallet.wallet.BRPeerManager
import timber.log.Timber
import java.math.BigDecimal
import java.util.Date

@Composable
fun TransactionRow(
    txItem: TxItem,
    ltcStats: LtcStats?,
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

    val amountReceived = BigDecimal(txItem.received).divide(BigDecimal(ONE_LITECOIN_OF_LITOSHIS))

    // ────────────────From working Develop branch─────────────────────

    // the current iso / fiat code
    val preferredCurrencyCode = BRSharedPrefs.getIsoSymbol(context)

    // the services amount
    val opsAmount: Long = run {
        if (txItem.outAmounts?.size != 3) return@run 0L
        txItem.outAmounts.minOrNull() ?: 0L
    }

    // the transactions amount
    val txAmount = BigDecimal(txItem.getReceived() - txItem.getSent())
        .abs().divide(BigDecimal(ONE_LITECOIN_OF_LITOSHIS))

    // determine sent vs received state
    val wasSentVsReceived = txItem.received - txItem.sent < 0

    // formatted transaction amount
    val amountString = if (wasSentVsReceived) String.format("-Ł $txAmount") else String.format("+Ł $txAmount")

    // LTC address to recipient or Brainwallet user
    val ltcAddress: String?

    // Output addresses
    val outputAddressSet = txItem.to.toHashSet()
    val opsSet = Utils.fetchServiceItem(context, ServiceItems.OPSALL)
        .split(",")
        .toHashSet()

    ltcAddress = outputAddressSet
        .filter { it !in opsSet }
        .filterNotNull()
        .firstOrNull() ?: "ERROR-ADDRESS"

    val toFromAddressFormatting: String = if (wasSentVsReceived) {
        String.format(stringResource(R.string.TransactionDetails_to), ltcAddress)
    } else {
        String.format(stringResource(R.string.TransactionDetails_from), ltcAddress)
    }

    val confirmationLevel: Int = run {
        val txBlockHeight = txItem.blockHeight
        val ltcStats = ltcStats ?: return
        val numberOfConfirmations = if (txBlockHeight == Integer.MAX_VALUE) {
            0
        } else {
            ltcStats.currentBlockHeight - txBlockHeight + 1
        }
        var level = 0
        if (numberOfConfirmations <= 0) {
            when (BRPeerManager.getRelayCount(txItem.txHash)) {
                0 -> 0
                1 -> 1
                else -> 2
            }
        } else {
            when {
                numberOfConfirmations == 1 -> 3
                numberOfConfirmations == 2 -> 4
                numberOfConfirmations == 3 -> 5
                else -> 6
            }
        }
    }

    val dateTimestamp: String = when (confirmationLevel) {
        0 -> ""
        1 -> ""
        2 -> ""
        else -> formatter.format(Date(txItem.timeStamp * 1000L))
    }

    val confirmationLabel: String = when (confirmationLevel) {
        0 -> "0%"
        1 -> "0%"
        2 -> "20%"
        3 -> "40%"
        4 -> "60%"
        5 -> "80%"
        6 -> "100%"
        else -> "∞"
    }
    val txIDBrowserURL: String = run {
        "${BWConstants.BLOCKCHAIR_EXPLORER_BASE_URL}${txItem.txHashHexReversed ?: ""}"
    }

    // ───────────────────────────From working Develop branch───────────────────────────

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
                    .padding(horizontal = 12.dp, vertical = 4.dp),
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

                Timber.d("confirmationLevel: $confirmationLevel")
                Text(
                    text = amountString,
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = if (wasSentVsReceived) DesignTheme.colors.error else DesignTheme.colors.affirm
                    ),
                    maxLines = 1
                )
            }
            Row(
                modifier = Modifier
                    .align(Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    modifier = Modifier
                        .fillMaxWidth(0.25f)
                        .padding(end = 12.dp),
                    text = confirmationLabel,
                    textAlign = TextAlign.End,
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        color = if (isDarkMode) {
                            Color.White.copy(0.8f)
                        } else {
                            Color.Black.copy(
                                0.8f
                            )
                        }
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.MiddleEllipsis
                )
                ConfirmationStatus(
                    modifier = Modifier
                        .height(16.dp)
                        .width(16.dp)
                        .padding(end = 80.dp),
                    numberOfConfs = confirmationLevel
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    modifier = Modifier
                        .fillMaxWidth(0.5f),
                    text = toFromAddressFormatting,
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
