package com.brainwallet.presenter.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.fragment.app.Fragment
import com.brainwallet.R

class FragmentMoonpay : Fragment() {

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.fragment_moonpay_temp, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            val webView = view.findViewById<WebView>(R.id.web_view)
            val btnClose = view.findViewById<Button>(R.id.close_button)

            webView.setInitialScale(80)
            webView.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                cacheMode = WebSettings.LOAD_DEFAULT
                setSupportZoom(true)
                useWideViewPort = true
                loadWithOverviewMode = true
            }

            // Load a URL
            webView.webViewClient = WebViewClient()
            webView.loadUrl("https://www.brainwallet.co/mobile-top-up.html")

            // Close button action
            btnClose.setOnClickListener {
                parentFragmentManager.popBackStack()
            }
        }
    }
