package com.brainwallet.ui.screens.main

import android.graphics.Bitmap
import android.view.ViewGroup
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

    Card(
        modifier = modifier
            .fillMaxSize()
            .background(brush = bentoDarkSurfaceGradient),
        shape = RoundedCornerShape(18.dp),
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )

                    setBackgroundColor(0)

                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.useWideViewPort = true

                    webViewClient = object : WebViewClient() {

                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                        }

                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                        }
                    }
                }
            },
            update = {
                it.loadUrl(url)
            }
        )
    }
}
