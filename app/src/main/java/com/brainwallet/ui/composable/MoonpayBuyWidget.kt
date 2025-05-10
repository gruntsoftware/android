package com.brainwallet.ui.composable


import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri

//TODO: wip here

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MoonpayBuyWidget(
    modifier: Modifier = Modifier,
    signedUrl: String = "https://buy.moonpay.com",
    onTransactionDetected: (String) -> Unit = {}
) {
    AndroidView(
        modifier = modifier.height(400.dp),
        factory = { ctx ->
            WebView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
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

//                    override fun shouldOverrideUrlLoading(
//                        view: WebView?,
//                        request: WebResourceRequest?
//                    ): Boolean {
//                        val url = request?.url?.toString() ?: return false
//
//                        if (url.contains("transactionId=")) {
//                            val uri = url.toUri()
//                            val transactionId = uri.getQueryParameter("transactionId")
//                            if (transactionId != null) {
//                                onTransactionDetected(transactionId)
//                                return true
//                            }
//                        }
//                        return false
//                    }
                }

            }
        },
        update = {
            it.loadUrl(signedUrl)
        }
    )

}