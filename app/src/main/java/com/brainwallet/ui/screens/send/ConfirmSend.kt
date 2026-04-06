
package com.brainwallet.ui.screens.send
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.R
import com.brainwallet.ui.composable.EditSendButton
import com.brainwallet.ui.theme.IBMPlexSans
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ConfirmSend(
    modifier: Modifier = Modifier,
    viewModel: SendViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val sectionBorder = 0.8.dp
    val sectionDarkColor = Color.White.copy(alpha = 0.3f)
    val sectionLightColor = Color.Black.copy(alpha = 0.95f)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight(0.8f)
                    .padding(start = 12.dp, end = 12.dp, bottom = 4.dp)
                    .border(
                        width = sectionBorder,
                        color = if (state.darkMode) sectionDarkColor else sectionLightColor,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .background(
                        color = if (state.darkMode) Color.Transparent else Color.White,
                        shape = RoundedCornerShape(16.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.width(8.dp))

                    SendDetailRow(
                        label = stringResource(R.string.amount_label),
                        valueLabel = state.amountInFiat
                    )
                    SendDetailRow(
                        label = stringResource(R.string.network_fees_label),
                        valueLabel = state.networkFees.toString()
                    )
                    SendDetailRow(
                        label = stringResource(R.string.service_fees_label),
                        valueLabel = state.serviceFees.toString()
                    )
                    SendDetailRow(
                        label = stringResource(R.string.amount_in_fiat_label),
                        valueLabel = state.amountInLTC.toString()
                    )
                    Text(
                        modifier = Modifier.padding(
                            start = 14.dp,
                            top = 10.dp,
                        ),
                        text = stringResource(R.string.memo_label),
                        style = TextStyle(
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Start,
                            color = if (state.darkMode) Color.White else Color.Black
                        )
                    )
                    Text(
                        modifier = Modifier.padding(
                            12.dp
                        ),
                        text = state.memo.ifEmpty { " " },
                        style = TextStyle(
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.Light,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Start,
                            color = if (state.darkMode) Color.White else Color.Black
                        ),
                        maxLines = 4
                    )

                    Spacer(modifier = Modifier.weight(0.5f))

                    HorizontalDivider(
                        modifier = Modifier.padding(
                            all = 12.dp
                        )
                    )
                    Text(
                        modifier = Modifier.padding(
                            start = 12.dp,
                        ),
                        text = stringResource(R.string.recipient_label),
                        style = TextStyle(
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Start,
                            color = if (state.darkMode) Color.White else Color.Black
                        )
                    )
                    Text(
                        modifier = Modifier.padding(
                            12.dp
                        ),
                        text = state.recipientLTCAddress.uppercase(),
                        style = TextStyle(
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.Light,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Start,
                            color = if (state.darkMode) Color.White else Color.Black
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            EditSendButton(
                modifier = modifier.padding(
                    start = 24.dp,
                    end = 24.dp,
                ),
                darkMode = state.darkMode,
                onClick = {
                }
            ) {
                Text(stringResource(R.string.edit_send_details))
            }
        }
    }
}
