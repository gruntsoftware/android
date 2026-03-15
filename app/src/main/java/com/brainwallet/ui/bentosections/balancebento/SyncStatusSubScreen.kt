package com.brainwallet.ui.bentosections.balancebento

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.R
import com.brainwallet.ui.theme.DesignTheme
import com.brainwallet.ui.theme.IBMPlexSans

@Composable
fun SyncStatusSubScreen(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
            .padding(bottom = 4.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Text(
            modifier = Modifier.padding(end = 2.dp)
                .align(Alignment.CenterVertically),
            text = stringResource(R.string.Send_title),
            style = TextStyle(
                fontFamily = IBMPlexSans,
                fontWeight = FontWeight.Light,
                fontSize = 11.sp,
                color = Color.White
            ),
            maxLines = 1

        )
        Icon(
            modifier = Modifier.padding(start = 4.dp, end = 4.dp)
                .align(Alignment.CenterVertically),
            painter = painterResource(R.drawable.cancel_24dp),
            contentDescription = "Cancel",
            tint = DesignTheme.colors.error
        )

        Text(
            modifier = Modifier.padding(start = 4.dp, end = 2.dp)
                .align(Alignment.CenterVertically),
            text = stringResource(R.string.Receive_title),
            style = TextStyle(
                fontFamily = IBMPlexSans,
                fontWeight = FontWeight.Light,
                fontSize = 11.sp,
                color = Color.White
            ),
            maxLines = 1

        )

        Icon(
            modifier = Modifier.padding(start = 4.dp)
                .align(Alignment.CenterVertically),
            painter = painterResource(id = R.drawable.check_circle_24dp),
            contentDescription = "Check circle send",
            tint = DesignTheme.colors.affirm
        )
    }
}
