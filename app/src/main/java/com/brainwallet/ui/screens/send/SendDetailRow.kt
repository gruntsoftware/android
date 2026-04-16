package com.brainwallet.ui.screens.send

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.ui.composable.DashedDivider
import com.brainwallet.ui.theme.IBMPlexSans
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SendDetailRow(
    label: String,
    valueLabel: String,
    modifier: Modifier = Modifier,
    viewModel: SendViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsState()
    val topBottomPad = 10.dp
    Row(
        modifier = modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.padding(
                start = 14.dp,
                top = topBottomPad,
                bottom = topBottomPad
            ),
            text = label,
            style = TextStyle(
                fontFamily = IBMPlexSans,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                textAlign = TextAlign.Start,
                color = if (state.darkMode) Color.White else Color.Black
            )
        )
        DashedDivider(
            modifier = Modifier.weight(1f)
                .align(Alignment.CenterVertically)
                .padding(topBottomPad),
            color = if (state.darkMode) Color.White else Color.Black,
            dashWidth = 2.dp,
            gapWidth = 4.dp
        )
        Text(
            modifier = Modifier.weight(0.38f)
                .padding(
                    end = 14.dp,
                    top = topBottomPad,
                    bottom = topBottomPad
                ),
            text = valueLabel,
            style = TextStyle(
                fontFamily = IBMPlexSans,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                textAlign = TextAlign.End,
                color = if (state.darkMode) Color.White else Color.Black
            )
        )
        Spacer(modifier = Modifier.width(1.dp))
    }
}
