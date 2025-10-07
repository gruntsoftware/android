package com.brainwallet.bento

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import com.brainwallet.bento.screen.BentoMainScreen
import ltd.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

class BentoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BrainwalletTheme(isSystemInDarkTheme()) {
                BentoMainScreen()
            }
        }
    }
}
