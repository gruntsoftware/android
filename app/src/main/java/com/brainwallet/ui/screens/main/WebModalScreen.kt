package com.brainwallet.ui.screens.main

import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.brainwallet.navigation.OnNavigate
import com.brainwallet.tools.manager.AnalyticsManager
import com.brainwallet.ui.composable.BrainwalletLoadingIndicator
import com.brainwallet.ui.theme.bentoDarkSurfaceGradient

@Composable
fun WebModalScreen(
    onNavigate: OnNavigate,
    url: String,
    modifier: Modifier = Modifier,
    invoiceCreated: (invoiceId: String, paymentUri: String) -> Unit = { _, _ -> }

) {
    val eventString = if (url.contains("bitrefill")) {
        "user_did_tap_shop_bento"
    } else {
        "user_did_tap_linktree"
    }
    LaunchedEffect(Unit) {
        AnalyticsManager.logCustomEvent(eventString)
    }

    var isLoading by remember { mutableStateOf(true) }

    Card(
        modifier = modifier.fillMaxSize(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = bentoDarkSurfaceGradient),
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
                                isLoading = true
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false

                                view?.evaluateJavascript(
                                    """
                                    (function() {
                                    function handleMessage(event) {
                                    try {
                                        var data = typeof event.data === 'string' ? JSON.parse(event.data) : event.data;
                                        if (data && data.event === 'invoice_created') {
                                            BitrefillAndroid.invoiceCreated(JSON.stringify(data));
                                        }
                                    } catch(e) {
                                        console.log('parse error:', e);
                                    }
                                    }
                                    window.addEventListener('message', handleMessage);
                                    document.addEventListener('message', handleMessage);
                                    })();
                                    """.trimIndent(),
                                    null
                                )
                            }
                        }

                        addJavascriptInterface(
                            WebAppInterface { invoiceId, paymentUri ->
                                invoiceCreated(invoiceId, paymentUri)
                            },
                            "BitrefillAndroid"
                        )
                    }
                },
                update = { it.loadUrl(url) }
            )

            if (isLoading) {
                BrainwalletLoadingIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

class WebAppInterface(
    private val onInvoiceCreated: (invoiceId: String, paymentUri: String) -> Unit
) {
    @JavascriptInterface
    fun invoiceCreated(dataJson: String) {
        try {
            val obj = org.json.JSONObject(dataJson)
            val invoiceId = obj.getString("invoiceId")
            val paymentUri = obj.getString("paymentUri")
            onInvoiceCreated(invoiceId, paymentUri)
        } catch (e: Exception) {
            android.util.Log.e("WebAppInterface", "Failed to parse invoice data", e)
        }
    }
}
