package com.brainwallet.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import com.brainwallet.tools.security.BRKeyStore
import com.brainwallet.tools.threads.BRExecutor
import com.brainwallet.ui.screens.home.MainScreen
import com.brainwallet.ui.theme.DesignTheme
import com.brainwallet.wallet.BRWalletManager
import timber.log.Timber

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

    override fun onResume() {
        super.onResume()
        initializeSyncIfReady()
    }

    private fun initializeSyncIfReady() {
        val masterPubKey = BRKeyStore.getMasterPublicKey(this)
        if (masterPubKey == null || masterPubKey.isEmpty()) {
            Timber.w("timber: initializeSyncIfReady: masterPubKey not set, skipping sync init")
            return
        }

        BRExecutor.getInstance().forBackgroundTasks().execute {
            if (!BRWalletManager.getInstance().isCreated()) {
                BRWalletManager.getInstance().initWallet(this@BentoActivity)
            }
            BRWalletManager.getInstance().refreshBalance(this@BentoActivity)
        }
    }

    companion object {
        fun start(context: Context, vararg flags: Int) {
            val intent = Intent(context, BentoActivity::class.java)
            flags.forEach { intent.addFlags(it) }
            context.startActivity(intent)
        }
    }
}
