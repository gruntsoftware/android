package com.brainwallet.ui.composable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.brainwallet.R
import com.brainwallet.ui.theme.DesignTheme
import com.brainwallet.ui.theme.IBMPlexSans

@Composable
fun MoonPayButtonCallout(
    modifier: Modifier = Modifier
) {
    val mainPadding = 22.dp
    val cornerRadius = 10.dp
    val calloutWidth = 190.dp
    val calloutHeight = 130.dp
    Box(
        modifier = Modifier
            .width(calloutWidth)
            .height(calloutHeight)
    ) {
        Column(
            modifier = Modifier
                .padding(top = mainPadding, bottom = mainPadding)
                .fillMaxSize(1f)
                .clip(RoundedCornerShape(cornerRadius))
                .background(DesignTheme.colors.background.copy(0.9f)),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier
                    .padding(all = 12.dp),
                text = stringResource(R.string.tap_moonpay_callout_title),
                style = TextStyle(
                    fontFamily = IBMPlexSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color.White,
                ),
                textAlign = TextAlign.Center
            )

            Image(
                modifier = Modifier
                    .fillMaxWidth(1f).padding(bottom = 5.dp),
                painter = painterResource(R.drawable.powered_by_moonpay_wht),
                contentDescription = "powered_by_moonpay_wht",
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Preview
@Composable
private fun CalloutWithPointersPreview() {
    Column {
        MoonPayButtonCallout()
    }
}
