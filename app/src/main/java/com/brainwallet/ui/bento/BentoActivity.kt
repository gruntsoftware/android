package com.brainwallet.ui.bento

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.toArgb
import com.brainwallet.design.presentation.state.rememberDarkModeState
import com.brainwallet.tools.threads.BRExecutor
import com.brainwallet.wallet.BRWalletManager
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme

class BentoActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val darkModeState = rememberDarkModeState()
            BrainwalletTheme(darkModeState.isDarkMode) {
                enableEdgeToEdge(
                    navigationBarStyle = SystemBarStyle.auto(
                        BrainwalletTheme.colors.background.toArgb(),
                        BrainwalletTheme.colors.background.toArgb(),
                        detectDarkMode = {
                            darkModeState.isDarkMode
                        }
                    )
                )
                BentoMainScreen()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!BRWalletManager.getInstance().isCreated()) {
            BRExecutor.getInstance().forBackgroundTasks()
                .execute(Runnable { BRWalletManager.getInstance().initWallet(this) })
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
