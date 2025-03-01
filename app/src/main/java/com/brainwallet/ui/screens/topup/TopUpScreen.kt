@file:OptIn(ExperimentalMaterial3Api::class)

package com.brainwallet.ui.screens.topup


import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brainwallet.R
import com.brainwallet.navigation.OnNavigate
import com.brainwallet.navigation.UiEffect
import com.brainwallet.tools.util.BRConstants
import com.brainwallet.ui.composable.BorderedLargeButton
import com.brainwallet.ui.composable.BrainwalletScaffold
import com.brainwallet.ui.composable.BrainwalletTopAppBar
import com.brainwallet.ui.composable.MediumTextButton
import com.brainwallet.ui.screens.yourseedproveit.YourSeedProveItEvent
import com.brainwallet.ui.screens.yourseedproveit.YourSeedProveItViewModel
import kotlinx.coroutines.delay

@Composable
fun TopUpScreen(
    onNavigate: OnNavigate,
    viewModel: YourSeedProveItViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    /// Layout values
    val leadingCopyPadding = 16
    val buttonRowHeight = 55
    val horizontalVerticalSpacing = 8
    val spacerHeight = 90
    val skipButtonWidth = 100

    var shouldShowWebView by remember { mutableStateOf(false) }
    var backEnabled by remember { mutableStateOf(false) }
    var shouldSkipBeVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(2000)
        shouldSkipBeVisible = true
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

        Spacer(modifier = Modifier.height(spacerHeight.dp))
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(horizontal = leadingCopyPadding.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(horizontalVerticalSpacing.dp),
        ) {

            if (!shouldShowWebView) {
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
            }
            if (shouldShowWebView) {
                AndroidView(
                    factory = {
                        WebView(it).apply {
                           setInitialScale(99)
                            settings.apply {
                                javaScriptEnabled = true
                                useWideViewPort = true
                                loadWithOverviewMode = true
                                builtInZoomControls = true
                                domStorageEnabled = true
                                allowContentAccess = true
                            }
                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                                    backEnabled = view.canGoBack()
                                }
                            }
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT)

                        }
                    },
                    update = {
                        it.loadUrl(BRConstants.MOBILE_MP_LINK)
                    },
                    modifier = Modifier
                        .height(600.dp)
                        .weight(1f)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(buttonRowHeight.dp),

                ){
                     Spacer(modifier = Modifier.weight(1f))
                    if(shouldSkipBeVisible) {
                        MediumTextButton(
                            onClick = {
                                viewModel.onEvent(YourSeedProveItEvent.OnGameAndSync)
                            },
                            modifier = Modifier
                                .width(skipButtonWidth.dp)
                                .padding(vertical = horizontalVerticalSpacing.dp),

                            ) {

                            Text(
                                text = stringResource(R.string.top_up_button_3),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Right
                            )
                        }
                    }
                }

            }
            if (!shouldShowWebView) {
                BorderedLargeButton(
                    onClick = {
                        shouldShowWebView = true
                    },
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
                    },
                    modifier = Modifier.fillMaxWidth()

                ) {
                    Text(
                        text = stringResource(R.string.top_up_button_2),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

            }

            Spacer(modifier = Modifier.weight(0.05f))
        }
    }
}

