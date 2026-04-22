package com.brainwallet.ui.screens.send

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.ui.composable.SendActionButton
import com.brainwallet.ui.theme.IBMPlexSans
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PreSendAddressRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    viewModel: SendViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()

    val sectionHeight = 68.dp
    val sectionBorder = 0.8.dp
    val sectionDarkColor = Color.White.copy(alpha = 0.3f)
    val sectionLightColor = Color.Black.copy(alpha = 0.95f)
    val onEvent = viewModel::onEvent

    val fieldTextStyle = TextStyle(
        fontFamily = IBMPlexSans,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        textAlign = TextAlign.Start,
    )
    val context = LocalContext.current

    LaunchedEffect(value) {
        onEvent(SendEvent.OnRecipientAddressChanged(value))
    }

    Box(
        modifier = Modifier
            .height(sectionHeight)
            .fillMaxSize()
            .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
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
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(0.65F)
            ) {
                TextField(
                    modifier = Modifier.height(sectionHeight),
                    value = value,
                    onValueChange = {
                        onEvent(SendEvent.OnRecipientAddressChanged(it))
                    },
                    singleLine = true,
                    textStyle = fieldTextStyle,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = if (state.darkMode) Color.White else Color.Black,
                        focusedTextColor = if (state.darkMode) {
                            if (state.isLTCAddressValid) Color.White else Color.Red
                        } else {
                            Color.Black
                        },
                        unfocusedTextColor = if (state.darkMode) Color.White else Color.Black,
                    ),
                    label = {
                        Text(label)
                    }

                )
            }
            Spacer(modifier = Modifier.weight(0.1f))
            SendActionButton(
                modifier = Modifier
                    .padding(
                        start = 8.dp,
                        end = 2.dp,
                    ),
                darkMode = state.darkMode,
                icon = Icons.Default.ContentPaste,
                onClick = { onEvent(SendEvent.OnTapPasteLTCAddress) }
            )
            Spacer(modifier = Modifier.width(4.dp))
            SendActionButton(
                modifier = Modifier
                    .padding(
                        start = 2.dp,
                        end = 4.dp
                    ),
                darkMode = state.darkMode,
                icon = Icons.Default.QrCode,
                onClick = { onEvent(SendEvent.OnTapShowCameraForQRLTCAddress) }
            )
            Spacer(modifier = Modifier.width(1.dp))
        }
    }
}
