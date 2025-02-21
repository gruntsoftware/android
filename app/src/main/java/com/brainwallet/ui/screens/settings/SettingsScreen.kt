@file:OptIn(ExperimentalMaterial3Api::class)

package com.brainwallet.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.brainwallet.R
import com.brainwallet.navigation.OnNavigate
import com.brainwallet.navigation.UiEffect
import com.brainwallet.tools.util.BRConstants
import com.brainwallet.ui.composable.BrainwalletScaffold
import com.brainwallet.ui.composable.BrainwalletTopAppBar
import com.brainwallet.ui.theme.BrainwalletTheme


@Composable
fun SettingsScreen(
    onNavigate: OnNavigate
) {
    val context = LocalContext.current

    /// Layout values
    val thinButtonFontSize = 14

    LaunchedEffect(Unit) {
        ///
    }

    BrainwalletScaffold(
        topBar = {
            BrainwalletTopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = { onNavigate.invoke(UiEffect.Navigate.Back()) },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                }
            )
        }

    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize(1f)
            .background(BrainwalletTheme.colors.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(BrainwalletTheme.colors.background),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {

                item {
                    Text(text = "Security")
                }
                item {
                    Text(text = "Language")
                }
                item {
                    Text(text = "Currency")
                }
                item {
                    Text(text = "Blockchain: Litecoin")
                }

                item {
                    Text(text = "Social")
                }

                item {
                    Text(text = "Lock")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            //app version
            Text(
            text = BRConstants.APP_VERSION_NAME_CODE,
        style = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
        fontSize = thinButtonFontSize.sp,
        fontWeight = FontWeight.Thin,
    )

        }
    }
}
@Preview
@Composable
fun SettingsScreenPreview() {
    SettingsScreen(onNavigate ={})
}