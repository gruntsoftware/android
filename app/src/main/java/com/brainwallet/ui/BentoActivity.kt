package com.brainwallet.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import com.brainwallet.ui.screens.home.MainScreen
import com.brainwallet.ui.theme.DesignTheme

class BentoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DesignTheme(isSystemInDarkTheme()) {
                MainScreen(onNavigate = {})
            }
            enableEdgeToEdge()
        }
    }

    companion object {
        fun start(context: Context, vararg flags: Int) {
            val intent = Intent(context, BentoActivity::class.java)
            flags.forEach {
                intent.addFlags(it)
            }
            context.startActivity(intent)
        }
    }
}
