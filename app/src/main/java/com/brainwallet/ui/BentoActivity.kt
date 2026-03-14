package com.brainwallet.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import com.brainwallet.ui.screens.home.MainScreen

class BentoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BrainwalletTheme(isSystemInDarkTheme()) {
                MainScreen(onNavigate = {})
            }
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
