@file:OptIn(ExperimentalMaterial3Api::class)

package com.brainwallet.ui.screens.topup


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brainwallet.R
import com.brainwallet.navigation.OnNavigate
import com.brainwallet.navigation.Route
import com.brainwallet.navigation.UiEffect
import com.brainwallet.tools.manager.AnalyticsManager
import com.brainwallet.tools.util.BRConstants
import com.brainwallet.ui.composable.BorderedLargeButton
import com.brainwallet.ui.composable.BrainwalletScaffold
import com.brainwallet.ui.composable.BrainwalletTopAppBar
import com.brainwallet.ui.screens.yourseedproveit.YourSeedProveItEvent
import com.brainwallet.ui.screens.yourseedproveit.YourSeedProveItViewModel

@Composable
fun TopUpScreen(
    onNavigate: OnNavigate,
    viewModel: YourSeedProveItViewModel = viewModel()
) {

    val context = LocalContext.current

    /// Layout values
    val leadingCopyPadding = 16
    val buttonRowHeight = 55
    val horizontalVerticalSpacing = 8
    val spacerHeight = 90
    val skipButtonWidth = 100

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
        Spacer(modifier = Modifier.height(spacerHeight.dp))
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = leadingCopyPadding.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(horizontalVerticalSpacing.dp),
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Row {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = stringResource(R.string.down_left_arrow),
                    modifier = Modifier
                        .rotate(45f)
                        .graphicsLayer(
                            scaleX = 2f,
                            scaleY = 2f
                        )
                )
                Spacer(modifier = Modifier.weight(1f))
            }

            Text(
                text = stringResource(R.string.top_up_title),
                style = MaterialTheme.typography.displaySmall.copy(textAlign = TextAlign.Left),
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = stringResource(R.string.top_up_detail_1),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )
            BorderedLargeButton(
                onClick = { onNavigate.invoke(UiEffect.Navigate(destinationRoute = Route.BuyLitecoin)) },
                modifier = Modifier.fillMaxWidth()

            ) {
                Text(
                    text = stringResource(R.string.top_up_button_1),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            BorderedLargeButton(
                onClick = {
                    viewModel.onEvent(YourSeedProveItEvent.OnGameAndSync)
                    AnalyticsManager.logCustomEvent(BRConstants._20250303_DSTU)
                },
                modifier = Modifier.fillMaxWidth()

            ) {
                Text(
                    text = stringResource(R.string.top_up_button_2),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.weight(0.05f))
        }
    }
}