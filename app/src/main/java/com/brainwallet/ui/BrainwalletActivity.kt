package com.brainwallet.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.brainwallet.navigation.MainNavHost
import com.brainwallet.navigation.Route
import com.brainwallet.presenter.activities.util.BRActivity
import com.brainwallet.tools.security.PostAuth
import com.brainwallet.ui.screens.inputwords.InputWordsViewModel.Companion.EFFECT_LEGACY_RECOVER_WALLET_AUTH
import com.brainwallet.ui.theme.BrainwalletAppTheme
import com.brainwallet.util.EventBus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

/**
 * Compose entry point here
 */
class BrainwalletActivity : BRActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val startDestination =
            intent.getSerializableExtra(EXTRA_START_DESTINATION) ?: Route.Welcome

        setContent {
            BrainwalletAppTheme {
                MainNavHost(
                    startDestination = startDestination,
                    onFinish = { finish() }
                )
            }
        }

        //communication from compose
        EventBus.events
            .map { it as EventBus.Event.Message }
            .onEach { event ->
                delay(70)
                if (event.message == EFFECT_LEGACY_RECOVER_WALLET_AUTH) {
                    PostAuth.getInstance().onRecoverWalletAuth(this@BrainwalletActivity, false)
                }
            }
            .launchIn(lifecycleScope)
    }

    companion object {
        private const val EXTRA_START_DESTINATION = "start_destination"

        fun createIntent(
            context: Context,
            startDestination: Route
        ) = Intent(context, BrainwalletActivity::class.java).apply {
            putExtra(EXTRA_START_DESTINATION, startDestination)
        }
    }
}