package com.brainwallet.design

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import com.brainwallet.design.screen.BentoMainScreen
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

class BentoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BrainwalletTheme(isSystemInDarkTheme()) {
                BentoMainScreen()
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
