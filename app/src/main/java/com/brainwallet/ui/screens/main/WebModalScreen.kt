package com.brainwallet.ui.screens.main

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.brainwallet.navigation.OnNavigate
import com.brainwallet.tools.manager.AnalyticsManager
import com.brainwallet.ui.theme.bentoDarkSurfaceGradient

@Composable
fun WebModalScreen(
    onNavigate: OnNavigate,
    url: String,
    modifier: Modifier = Modifier,
) {
    val eventString = if (url.contains("bitrefill")) {
        "user_did_tap_bitrefill"
    } else {
        "user_did_tap_linktree"
    }
    LaunchedEffect(Unit) {
        AnalyticsManager.logCustomEvent(eventString)
    }
    val context = LocalContext.current
    Card(
        modifier = modifier
            .fillMaxSize()
            .background(brush = bentoDarkSurfaceGradient),
        shape = RoundedCornerShape(18.dp),
    ) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    loadUrl(url)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
