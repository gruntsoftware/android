/*
 * Copyright (c) 2025 Sanjaya Inc. All rights reserved.
 */

package com.brainwallet.ltc.presentation.component.transactionhistory

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.brainwallet.design.presentation.component.effect.CardOpacityContainer
import com.brainwallet.ltc.R
import com.brainwallet.ltc.domain.model.TxItem
import com.brainwallet.ltc.domain.usecase.GenerateQrCodeUseCase
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme
import com.grunt.brainwallet.core.presentation.util.toLtcStringFormatted
import org.koin.compose.koinInject
import kotlin.math.abs

data class TransactionHistoryDetailState(
    val transaction: TxItem,
    val addressForQr: String? = null,
    val qrCode: Bitmap? = null,
)

@Composable
fun rememberTransactionDetailState(
    transaction: TxItem,
    generateQrCodeUseCase: GenerateQrCodeUseCase = koinInject()
): TransactionHistoryDetailState {
    val addressForQr = remember(transaction) {
        transaction.to.firstOrNull { it.isNotEmpty() } ?: ""
    }
    val qrBitmap = remember(addressForQr, generateQrCodeUseCase) {
        if (addressForQr.isNotEmpty()) {
            generateQrCodeUseCase.generateQrCode("litecoin:$addressForQr")
        } else {
            null
        }
    }
    return TransactionHistoryDetailState(
        transaction = transaction,
        addressForQr = addressForQr,
        qrCode = qrBitmap
    )
}

@Composable
fun TransactionDetailContent(
    state: TransactionHistoryDetailState,
    onExportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transaction = state.transaction
    CardOpacityContainer(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CardOpacityContainer(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    LabelValueRow(
                        label = stringResource(R.string.ltc_transaction_fee_label),
                        value = stringResource(
                            R.string.ltc_transaction_fee_value,
                            transaction.fee.toLtcStringFormatted()
                        ),
                        isDestructive = true
                    )

                    LabelValueRow(
                        label = stringResource(R.string.ltc_transaction_starting_balance_label),
                        value = stringResource(
                            R.string.ltc_transaction_balance_value,
                            abs(
                                transaction.balanceAfterTx + transaction.sent + transaction.fee - transaction.received
                            ).toLtcStringFormatted()
                        )
                    )

                    LabelValueRow(
                        label = stringResource(R.string.ltc_transaction_ending_balance_label),
                        value = stringResource(
                            R.string.ltc_transaction_balance_value,
                            transaction.balanceAfterTx.toLtcStringFormatted()
                        )
                    )

                    val addressToDisplay = transaction.to.firstOrNull { it.isNotEmpty() }
                        ?: transaction.from.firstOrNull()
                        ?: ""

                    if (addressToDisplay.isNotEmpty()) {
                        LabelValueRow(
                            label = stringResource(R.string.ltc_transaction_to_label),
                            value = addressToDisplay,
                            isMultiline = true
                        )
                    }
                }
            }

            CardOpacityContainer(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    state.qrCode?.asImageBitmap()?.let { imageBitmap ->
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = stringResource(R.string.ltc_transaction_qr_code_content_description),
                            modifier = Modifier
                                .width(80.dp)
                                .aspectRatio(1f),
                            colorFilter = ColorFilter.tint(BrainwalletTheme.colors.content)
                        )
                    } ?: Box(
                        modifier = Modifier
                            .width(80.dp)
                            .aspectRatio(1f)
                            .background(
                                color = BrainwalletTheme.colors.content.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            )
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(
                                R.string.ltc_transaction_blockheight_label,
                                transaction.blockHeight
                            ),
                            style = BrainwalletTheme.typography.bodyMedium.copy(
                                color = BrainwalletTheme.colors.content
                            )
                        )

                        if (!state.addressForQr.isNullOrBlank()) {
                            Text(
                                text = state.addressForQr,
                                style = BrainwalletTheme.typography.bodySmall.copy(
                                    color = BrainwalletTheme.colors.content.copy(alpha = 0.8f)
                                ),
                                maxLines = 3
                            )
                        }
                    }
                }
            }

            CardOpacityContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExportClick() }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.ltc_transaction_export_data_button),
                        style = BrainwalletTheme.typography.titleMedium.copy(
                            color = BrainwalletTheme.colors.content,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun LabelValueRow(
    label: String,
    value: String,
    isDestructive: Boolean = false,
    isMultiline: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = if (isMultiline) Alignment.Top else Alignment.Bottom
    ) {
        Text(
            text = label,
            style = BrainwalletTheme.typography.bodyMedium.copy(
                color = BrainwalletTheme.colors.content.copy(alpha = 0.5f)
            ),
            modifier = Modifier.padding(bottom = if (isMultiline) 4.dp else 0.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Dashed Line
        if (!isMultiline) {
            DashedDivider(
                color = BrainwalletTheme.colors.content.copy(alpha = 0.3f),
                modifier = Modifier
                    .weight(1f)
                    .padding(bottom = 5.dp) // Align with text baseline roughly
            )
        } else {
            DashedDivider(
                color = BrainwalletTheme.colors.content.copy(alpha = 0.3f),
                modifier = Modifier
                    .weight(1f)
                    .padding(top = 10.dp) // Push down slightly for multiline top alignment
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = value,
            style = BrainwalletTheme.typography.bodyMedium.copy(
                color = if (isDestructive) BrainwalletTheme.colors.error else BrainwalletTheme.colors.content,
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun DashedDivider(
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier,
    dotSize: Dp = 2.dp,
    gapSize: Dp = 4.dp
) {
    val density = LocalDensity.current
    val dotSizePx = with(density) { dotSize.toPx() }
    val gapSizePx = with(density) { gapSize.toPx() }

    Canvas(modifier = modifier.height(dotSize)) {
        val width = size.width
        val totalDashWidth = dotSizePx + gapSizePx
        val count = (width / totalDashWidth).toInt()

        for (i in 0 until count) {
            drawCircle(
                color = color,
                radius = dotSizePx / 2,
                center = Offset(x = i * totalDashWidth + (dotSizePx / 2), y = center.y)
            )
        }
    }
}

@Composable
@Preview
private fun TransactionDetailContentPreview() {
    val mockTransaction = TxItem(
        timeStamp = 1730059740L,
        blockHeight = 2920025,
        txHash = byteArrayOf(),
        txReversed = "abc123reversed",
        sent = 400000L,
        received = 0L,
        fee = 400L,
        to = listOf("ltc1tytgg98bbdfbciudbvn0uabvijbowiaebv08ba80rebv0wrbev08b"),
        from = listOf("ltc1sender123456789"),
        balanceAfterTx = 613422989L,
        txSize = 250,
        outAmounts = listOf(400000L),
        isValid = true
    )
    val state = TransactionHistoryDetailState(mockTransaction)

    BrainwalletTheme(isSystemInDarkTheme()) {
        Box(modifier = Modifier.background(BrainwalletTheme.colors.surface)) {
            TransactionDetailContent(
                state = state,
                onExportClick = {}
            )
        }
    }
}
