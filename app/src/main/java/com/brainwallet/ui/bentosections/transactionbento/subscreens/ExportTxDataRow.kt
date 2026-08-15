package com.brainwallet.ui.bentosections.transactionbento.subscreens
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.R
import com.brainwallet.constants.bentoBorderWidth
import com.brainwallet.constants.buttonCornerRadius
import com.brainwallet.constants.exportTxRowHt
import com.brainwallet.ui.theme.IBMPlexSans
import com.brainwallet.ui.theme.bentoDarkBorderGradient
import com.brainwallet.ui.theme.bentoDarkSurfaceGradient
import com.brainwallet.ui.theme.bentoLightBorderGradient
import com.brainwallet.ui.theme.bentoLightSurfaceGradient

@Composable
fun ExportTxDataRow(
    isDarkMode: Boolean,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Box(
        modifier = Modifier
            .height(exportTxRowHt)
            .background(
                brush = if (isDarkMode) bentoLightSurfaceGradient else bentoDarkSurfaceGradient,
                shape = RoundedCornerShape(buttonCornerRadius)
            )
            .border(
                width = bentoBorderWidth,
                brush = if (isDarkMode) bentoDarkBorderGradient else bentoLightBorderGradient,
                shape = RoundedCornerShape(buttonCornerRadius)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    modifier = Modifier
                        .padding(top = 2.dp, bottom = 2.dp)
                        .align(Alignment.CenterHorizontally),
                    text = stringResource(R.string.export_transaction_button_title),
                    style = TextStyle(
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    ),
                    maxLines = 1
                )
            }
        }
    }
}
