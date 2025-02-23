package com.brainwallet.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.brainwallet.BrainwalletApp
import com.brainwallet.R
import com.brainwallet.data.model.AppSetting
import com.brainwallet.navigation.LegacyNavigation
import com.brainwallet.navigation.MainNavHost
import com.brainwallet.navigation.Route
import com.brainwallet.presenter.activities.util.BRActivity
import com.brainwallet.tools.animation.BRAnimator
import com.brainwallet.tools.animation.BRDialog
import com.brainwallet.tools.manager.AnalyticsManager
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.security.AuthManager
import com.brainwallet.tools.security.BRKeyStore
import com.brainwallet.tools.security.PostAuth
import com.brainwallet.tools.security.SmartValidator
import com.brainwallet.tools.util.BRConstants
import com.brainwallet.tools.util.BRConstants.BW_PIN_LENGTH
import com.brainwallet.tools.util.Utils
import com.brainwallet.ui.screens.inputwords.InputWordsViewModel.Companion.EFFECT_LEGACY_RECOVER_WALLET_AUTH
import com.brainwallet.ui.screens.inputwords.InputWordsViewModel.Companion.LEGACY_DIALOG_INVALID
import com.brainwallet.ui.screens.inputwords.InputWordsViewModel.Companion.LEGACY_DIALOG_WIPE_ALERT
import com.brainwallet.ui.screens.inputwords.InputWordsViewModel.Companion.LEGACY_EFFECT_RESET_PIN
import com.brainwallet.ui.screens.yourseedproveit.YourSeedProveItViewModel.Companion.LEGACY_EFFECT_ON_PAPERKEY_PROVED
import com.brainwallet.ui.screens.yourseedwords.YourSeedWordsViewModel.Companion.LEGACY_EFFECT_ON_SAVED_PAPERKEY
import com.brainwallet.ui.theme.BrainwalletAppTheme
import com.brainwallet.util.EventBus
import com.brainwallet.wallet.BRWalletManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import timber.log.Timber


/**
 * Compose entry point here
 */
class BrainwalletActivity : BRActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val startDestination =
            intent.getSerializableExtra(EXTRA_START_DESTINATION) ?: Route.Welcome

        if (startDestination is Route.UnLock) {
            onCheckPin()
        }

        if (startDestination is Route.Welcome) {
            onLegacyLogic()
        }

        setContent {
            val appSetting by BrainwalletApp.module.settingRepository.settings.collectAsState(
                AppSetting()
            )

            BrainwalletAppTheme(darkTheme = appSetting.isDarkMode) {
                MainNavHost(
                    startDestination = startDestination,
                    onFinish = { finish() }
                )
            }
        }

        /**
         * Communication between compose and legacy logic using the following event bus
         * why we are using this event bus?
         * we need to migrate gradually to compose, so that's why we still use legacy logic here
         * from compose just send event using this EventBus
         */
        EventBus.events
            .onEach { event ->
                delay(70)
                when (event) {
                    is EventBus.Event.Message -> {
                        when (event.message) {
                            EFFECT_LEGACY_RECOVER_WALLET_AUTH -> {
                                PostAuth.getInstance()
                                    .onRecoverWalletAuth(this@BrainwalletActivity, false)
                            }

                            LEGACY_EFFECT_RESET_PIN -> {
                                /**
                                 * when the wallet disabled after wrong passcode/pin
                                 * we can enable by reset the pin
                                 */
                                AuthManager.getInstance().setPinCode("", this)
                                createIntent(
                                    context = this,
                                    startDestination = Route.SetPasscode()
                                ).apply {
                                    putExtra("noPin", true)
                                    flags =
                                        Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                }.also {
                                    startActivity(it)
                                }
                            }

                            LEGACY_EFFECT_ON_SAVED_PAPERKEY -> {
                                PostAuth.getInstance().onPhraseProveAuth(this, false)
                            }

                            LEGACY_EFFECT_ON_PAPERKEY_PROVED -> {
                                BRSharedPrefs.putPhraseWroteDown(this@BrainwalletActivity, true)
                                LegacyNavigation.startBreadActivity(
                                    this@BrainwalletActivity,
                                    false
                                )
                                finishAffinity()
                            }

                            LEGACY_DIALOG_INVALID -> BRDialog.showCustomDialog(
                                BrainwalletApp.getBreadContext(),
                                "",
                                getString(R.string.RecoverWallet_invalid),
                                getString(R.string.AccessibilityLabels_close),
                                null,
                                { brDialogView ->
                                    brDialogView.dismissWithAnimation()
                                    BRDialog.hideDialog()
                                },
                                null,
                                null,
                                0
                            )

                            LEGACY_DIALOG_WIPE_ALERT -> BRDialog.showCustomDialog(
                                this,
                                getString(R.string.WipeWallet_alertTitle),
                                getString(R.string.WipeWallet_alertMessage),
                                getString(R.string.WipeWallet_wipe),
                                getString(R.string.Button_cancel),
                                { brDialogView ->
                                    brDialogView.dismissWithAnimation()
                                    val m = BRWalletManager.getInstance()
                                    m.wipeWalletButKeystore(this@BrainwalletActivity)
                                    m.wipeKeyStore(this@BrainwalletActivity)

                                    createIntent(this@BrainwalletActivity).apply {
                                        flags =
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                    }.also { startActivity(it) }

                                },
                                { brDialogView -> brDialogView.dismissWithAnimation() },
                                null,
                                0
                            )

                        }
                    }

                    is EventBus.Event.LegacyPasscodeVerified -> onPasscodeVerified(event.passcode)
                    is EventBus.Event.LegacyUnLock -> onUnlock(event.passcode)
                }
            }
            .launchIn(lifecycleScope)
    }


    /**
     * legacy logic, when the pin/passcode empty (not set)
     * then should go to setpasscode
     */
    private fun onCheckPin() {
        val pin = BRKeyStore.getPinCode(this)
        if (pin.isEmpty() && pin.length != BW_PIN_LENGTH) {
            lifecycleScope.launch {
                EventBus.emit(
                    EventBus.Event.Message(
                        LEGACY_EFFECT_RESET_PIN
                    )
                )
            }
        }
    }

    /**
     * provide old logic to use compose unlock screen instead of LoginActivity
     */
    private fun onUnlock(passcode: List<Int>) {
        if (AuthManager.getInstance().checkAuth(passcode.joinToString(""), this)) {
            AuthManager.getInstance().authSuccess(this)
            AnalyticsManager.logCustomEvent(BRConstants._20200217_DUWB)
            AnalyticsManager.logCustomEvent(BRConstants._20200217_DUWB)

            LegacyNavigation.startBreadActivity(this, false)
        } else {
            AuthManager.getInstance().authFail(this)
            //for now just toast
            Toast.makeText(this, R.string.incorrect_passcode, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * this is old logic
     */
    private fun onPasscodeVerified(passcode: List<Int>) {
        AuthManager.getInstance().authSuccess(this)
        AuthManager.getInstance().setPinCode(passcode.joinToString(separator = ""), this)
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

        @JvmStatic
        fun createIntent(
            context: Context,
            startDestination: Route = Route.Welcome
        ) = Intent(context, BrainwalletActivity::class.java).apply {
            putExtra(EXTRA_START_DESTINATION, startDestination)
        }
    }
}