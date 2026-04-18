package com.brainwallet.ui.bentosections.tutorials

import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.brainwallet.ui.screens.main.MainViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.ui.theme.IBMPlexSans
import com.brainwallet.ui.theme.mainBentoSurface
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TutorialsBentoScreen(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    viewModel: MainViewModel = koinViewModel()
) {
    val context = LocalContext.current
    val appSetting by viewModel.appSetting.collectAsStateWithLifecycle()
    val isDarkMode = appSetting.isDarkMode

    Box(
        modifier = Modifier
            .fillMaxSize()
            .mainBentoSurface(isDarkMode)
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = stringResource(com.brainwallet.R.string.tutorials_label).uppercase(),
                color = if (isDarkMode) Color.White else Color.Black,
                style = TextStyle(
                    fontFamily = IBMPlexSans,
                    fontWeight = FontWeight.Normal,
                    fontSize = 10.sp
                ),
                modifier = Modifier
                    .padding(start = 1.dp, end = 1.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(6.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
        }

        Text(
            text = stringResource(com.brainwallet.R.string.coming_soon),
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp)
                .wrapContentSize(Alignment.Center),
            style = TextStyle(
                fontFamily = IBMPlexSans,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = if (isDarkMode) Color.White else Color.Black,
                textAlign = TextAlign.Center
            ),
        )
    }
}

@PreviewLightDark
@Composable
private fun TutorialsBentoScreenPreview() {
    Box(modifier = Modifier) {
        TutorialsBentoScreen()
    }
}
