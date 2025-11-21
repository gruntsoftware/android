package com.brainwallet.ui.bento

import android.content.Context
import android.content.Intent
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.lifecycleScope
import com.brainwallet.R
import com.brainwallet.design.presentation.state.rememberDarkModeState
import com.brainwallet.navigation.LegacyNavigation.restartBreadActivity
import com.brainwallet.navigation.LegacyNavigation.startBreadActivity
import com.brainwallet.navigation.MainNavHost
import com.brainwallet.navigation.Route
import com.brainwallet.navigation.Route.UnLock
import com.brainwallet.presenter.activities.BreadActivity
import com.brainwallet.presenter.activities.settings.SyncBlockchainActivity
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.security.PostAuth
import com.brainwallet.ui.BrainwalletActivity.Companion.createIntent
import com.brainwallet.ui.screens.home.SettingsViewModel
import com.brainwallet.util.EventBus
import com.grunt.brainwallet.core.presentation.theme.BrainwalletTheme
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class BentoActivity : BreadActivity() {

    override fun setupContentView() {
        observeLegacyMenuEvent()
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
                MainNavHost(startDestination = Route.BentoMainScreen, onFinish = {
                    finish()
                })
            }
        }
    }

    private fun observeLegacyMenuEvent() {
        EventBus.events
            .filter { it is EventBus.Event.Message }
            .map { it as EventBus.Event.Message }
            .onEach { handleLegacyMessage(it.message) }
            .launchIn(lifecycleScope)
    }

    private fun handleLegacyMessage(message: String) {
        when (message) {
            SettingsViewModel.LEGACY_EFFECT_ON_LOCK -> {
                startBreadActivity(this, true)
            }
            SettingsViewModel.LEGACY_EFFECT_ON_TOGGLE_DARK_MODE -> {
                restartBreadActivity(this)
            }
            SettingsViewModel.LEGACY_EFFECT_ON_SEC_UPDATE_PIN -> {
                createIntent(this, UnLock(true)).apply {
                    putExtra("noPin", true)
                    startActivity(this)
                }
            }
            SettingsViewModel.LEGACY_EFFECT_ON_SEED_PHRASE -> {
                PostAuth.getInstance().onPhraseCheckAuth(this, true)
            }
            SettingsViewModel.LEGACY_EFFECT_ON_SHARE_ANALYTICS_DATA_TOGGLE -> {
                val isEnabled = BRSharedPrefs.getShareData(this)
                BRSharedPrefs.putShareData(this, !isEnabled)
            }
            SettingsViewModel.LEGACY_EFFECT_ON_SYNC -> {
                startActivity(Intent(this, SyncBlockchainActivity::class.java))
                overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
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
