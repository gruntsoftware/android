package com.brainwallet.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.brainwallet.R
import com.brainwallet.navigation.LegacyNavigation
import com.brainwallet.navigation.MainNavHost
import com.brainwallet.navigation.Route
import com.brainwallet.presenter.activities.util.BRActivity
import com.brainwallet.tools.animation.BRAnimator
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.security.AuthManager
import com.brainwallet.tools.security.BRKeyStore
import com.brainwallet.tools.security.PostAuth
import com.brainwallet.tools.security.SmartValidator
import com.brainwallet.tools.util.Utils
import com.brainwallet.ui.screens.inputwords.InputWordsViewModel.Companion.EFFECT_LEGACY_RECOVER_WALLET_AUTH
import com.brainwallet.ui.screens.yourseedproveit.YourSeedProveItViewModel.Companion.LEGACY_EFFECT_ON_PAPERKEY_PROVED
import com.brainwallet.ui.screens.yourseedwords.YourSeedWordsViewModel.Companion.LEGACY_EFFECT_ON_SAVED_PAPERKEY
import com.brainwallet.ui.theme.BrainwalletAppTheme
import com.brainwallet.util.EventBus
import com.brainwallet.wallet.BRWalletManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import timber.log.Timber


/**
 * Compose entry point here
 */
class BrainwalletActivity : BRActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val startDestination =
            intent.getSerializableExtra(EXTRA_START_DESTINATION) ?: Route.Welcome

        if (startDestination is Route.Welcome) {
            onLegacyLogic()
        }

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
            .onEach { event ->
                delay(70)
                when (event) {
                    is EventBus.Event.Message -> {
                        if (event.message == EFFECT_LEGACY_RECOVER_WALLET_AUTH) {
                            PostAuth.getInstance()
                                .onRecoverWalletAuth(this@BrainwalletActivity, false)
                        } else if (event.message == LEGACY_EFFECT_ON_SAVED_PAPERKEY) {
                            PostAuth.getInstance().onPhraseProveAuth(this, false)
                        } else if (event.message == LEGACY_EFFECT_ON_PAPERKEY_PROVED) {
                            BRSharedPrefs.putPhraseWroteDown(this@BrainwalletActivity, true)
                            LegacyNavigation.startBreadActivity(
                                this@BrainwalletActivity,
                                false
                            )
                            finishAffinity()
                        }
                    }

                    is EventBus.Event.LegacyPasscodeVerified -> onPasscodeVerified(event.passcode)
                }
            }
            .launchIn(lifecycleScope)
    }

    /**
     * this is old logic
     */
    private fun onPasscodeVerified(passcode: List<Int>) {
        AuthManager.getInstance().authSuccess(this)
        AuthManager.getInstance().setPinCode(passcode.joinToString(), this)
        if (intent.getBooleanExtra("noPin", false)) {
            LegacyNavigation.startBreadActivity(this, false)
        } else {
            BRAnimator.showBreadSignal(
                this,
                getString(R.string.Alerts_pinSet),
                getString(R.string.UpdatePin_createInstruction),
                R.drawable.ic_check_mark_white
            ) { PostAuth.getInstance().onCreateWalletAuth(this, false) }
        }
    }

    /**
     * describe [onLegacyLogic]
     * this will be using the old logic from the IntroActivity (already gone)
     */
    private fun onLegacyLogic() {
        if (Utils.isEmulatorOrDebug(this)) Utils.printPhoneSpecs()

        val masterPubKey = BRKeyStore.getMasterPublicKey(this)
        var isFirstAddressCorrect = false
        if (masterPubKey != null && masterPubKey.isNotEmpty()) {
            Timber.d("timber: masterPubkey exists")

            isFirstAddressCorrect = SmartValidator.checkFirstAddress(this, masterPubKey)
        }
        if (!isFirstAddressCorrect) {
            Timber.d("timber: Calling wipeWalletButKeyStore")
            BRWalletManager.getInstance().wipeWalletButKeystore(this)
        }

        /**
         * inside the following it will handle navigate to old activity [com.brainwallet.presenter.activities.BreadActivity]
         */
        PostAuth.getInstance().onCanaryCheck(this, false)
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