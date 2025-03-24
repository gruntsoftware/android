package com.brainwallet.presenter.fragments

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.brainwallet.R
import com.brainwallet.tools.manager.BRClipboardManager
import com.brainwallet.tools.manager.BRSharedPrefs

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
            val buttonClose = view.findViewById<ImageButton>(R.id.close_button)
            val copyAddressButton = view.findViewById<ImageButton>(R.id.copy_button)
            var receiveAddressLabel = view.findViewById<TextView>(R.id.receive_address)
            val addressText = BRSharedPrefs.getReceiveAddress(context)

           // buttonClose
            receiveAddressLabel.setText(addressText)
            receiveAddressLabel.setTextColor(receiveAddressLabel.getResources().getColor(R.color.midnight, null));
            webView.setInitialScale(80)
            webView.settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                cacheMode = WebSettings.LOAD_DEFAULT
                setSupportZoom(true)
                useWideViewPort = true
                loadWithOverviewMode = true
            }

            webView.webViewClient = WebViewClient()
            webView.loadUrl("https://www.brainwallet.co/mobile-top-up.html")

            buttonClose.setOnClickListener {
                parentFragmentManager.popBackStack()
            }

            receiveAddressLabel.setOnClickListener {
                BRClipboardManager.putClipboard(context, receiveAddressLabel.getText().toString())
            }

            copyAddressButton.setOnClickListener {
                BRClipboardManager.putClipboard(context, receiveAddressLabel.getText().toString())
            }
        }
    }
