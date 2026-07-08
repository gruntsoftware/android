package com.brainwallet.ui

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.brainwallet.BrainwalletApp
import com.brainwallet.R
import com.brainwallet.constants.BWConstants
import com.brainwallet.constants.BWConstants.BW_PIN_LENGTH
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.navigation.LegacyNavigation
import com.brainwallet.navigation.LegacyNavigation.restartBreadActivity
import com.brainwallet.navigation.LegacyNavigation.startBrainwalletActivity
import com.brainwallet.navigation.MainNavigationHost
import com.brainwallet.navigation.Route
import com.brainwallet.navigation.Route.UnLock
import com.brainwallet.presenter.activities.settings.SyncBlockchainActivity
import com.brainwallet.presenter.activities.util.BRActivity
import com.brainwallet.tools.animation.BRAnimator
import com.brainwallet.tools.animation.BRDialog
import com.brainwallet.tools.manager.AnalyticsManager
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.manager.InternetManager
import com.brainwallet.tools.manager.sync.SyncThreadManager
import com.brainwallet.tools.security.AuthManager
import com.brainwallet.tools.security.BRKeyStore
import com.brainwallet.tools.security.PostAuth
import com.brainwallet.tools.security.SmartValidator
import com.brainwallet.tools.sqlite.TransactionDataSource
import com.brainwallet.tools.threads.BRExecutor
import com.brainwallet.tools.util.Utils
import com.brainwallet.ui.screens.restore.RestoreViewModel.Companion.EFFECT_LEGACY_RECOVER_WALLET_AUTH
import com.brainwallet.ui.screens.restore.RestoreViewModel.Companion.LEGACY_DIALOG_INVALID
import com.brainwallet.ui.screens.restore.RestoreViewModel.Companion.LEGACY_DIALOG_WIPE_ALERT
import com.brainwallet.ui.screens.restore.RestoreViewModel.Companion.LEGACY_EFFECT_RESET_PIN
import com.brainwallet.ui.screens.settings.SettingsViewModel
import com.brainwallet.ui.screens.yourseedproveit.YourSeedProveItViewModel.Companion.LEGACY_EFFECT_ON_PAPERKEY_PROVED
import com.brainwallet.ui.theme.BrainwalletAppTheme
import com.brainwallet.util.EventBus
import com.brainwallet.wallet.BRPeerManager
import com.brainwallet.wallet.BRWalletManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber
import com.badlogic.gdx.backends.android.AndroidFragmentApplication
/**
 * Compose entry point here
 */
class BrainwalletActivity :
    BRActivity(),
    BRWalletManager.OnBalanceChanged,
    BRPeerManager.OnTxStatusUpdate,
    TransactionDataSource.OnTxAddedListener,
    InternetManager.ConnectionReceiverListener,
    AndroidFragmentApplication.Callbacks {

    override fun exit() { android.util.Log.d("GDX", "Callbacks.exit() called") }

    private val settingRepository by inject<SettingRepository>()
    private var mConnectionReceiver: InternetManager? = null
    var appVisible: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        onConnectionChanged(InternetManager.getInstance().isConnected(this))
        BRSharedPrefs.getLTCViewingPreference(application)

        val startDestination =
            intent.getSerializableExtra(EXTRA_START_DESTINATION) ?: Route.Welcome

        if (startDestination is Route.UnLock) {
            onCheckPin()
        }

        if (startDestination is Route.Welcome) {
            onLegacyLogic()
        }

        setContent {
            val appSetting by settingRepository.settings.collectAsState(
                AppSetting()
            )
            enableEdgeToEdge()
            BrainwalletAppTheme(appSetting = appSetting) {
                MainNavigationHost(
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
                    is EventBus.Event.Message -> handleLegacyMessage(event.message)
                    is EventBus.Event.LegacyPasscodeVerified -> onPasscodeVerified(event.passcode)
                    is EventBus.Event.LegacyUnLock -> onUnlock(event.passcode)
                    is EventBus.Event.QRCodeScanned -> Unit
                }
            }
            .launchIn(lifecycleScope)
    }
    private fun handleLegacyMessage(message: String) {
        when (message) {
            SettingsViewModel.LEGACY_EFFECT_ON_LOCK -> {
                startBrainwalletActivity(this, true)
            }
            SettingsViewModel.LEGACY_EFFECT_ON_TOGGLE_DARK_MODE -> {
                restartBreadActivity(this)
            }
            SettingsViewModel.LEGACY_EFFECT_ON_SEC_UPDATE_PIN -> {
                createIntent(this, UnLock(true))
                    .apply { putExtra("noPin", true) }
                    .also { startActivity(it) }
            }
            SettingsViewModel.LEGACY_EFFECT_ON_SEED_PHRASE -> {
                PostAuth.getInstance().onPhraseCheckAuth(this@BrainwalletActivity, true)
            }
            SettingsViewModel.LEGACY_EFFECT_ON_SHARE_ANALYTICS_DATA_TOGGLE -> {
                val current = BRSharedPrefs.getShareData(this)
                BRSharedPrefs.putShareData(this, !current)
            }
            SettingsViewModel.LEGACY_EFFECT_ON_SYNC -> {
                startActivity(Intent(this, SyncBlockchainActivity::class.java))
            }
            EFFECT_LEGACY_RECOVER_WALLET_AUTH -> {
                PostAuth.getInstance().onRecoverWalletAuth(this@BrainwalletActivity, false)
            }
            LEGACY_EFFECT_RESET_PIN -> {
                AuthManager.getInstance().setPinCode("", this)
                createIntent(
                    context = this,
                    startDestination = Route.SetPasscode()
                ).apply {
                    putExtra("noPin", true)
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                }.also { startActivity(it) }
            }
            LEGACY_EFFECT_ON_PAPERKEY_PROVED -> {
                BRSharedPrefs.putPhraseWroteDown(this@BrainwalletActivity, true)
            }
            LEGACY_DIALOG_INVALID -> BRDialog.showCustomDialog(
                BrainwalletApp.breadContext,
                "",
                getString(R.string.RecoverWallet_invalid),
                getString(R.string.AccessibilityLabels_close),
                null,
                { brDialogView ->
                    brDialogView.dismissWithAnimation()
                    BRDialog.hideDialog()
                },
                null, null, 0
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
                    m.wipeWalletButKeystore(this)
                    m.wipeKeyStore(this)
                    createIntent(this).apply {
                        flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                    }.also { startActivity(it) }
                },
                { brDialogView -> brDialogView.dismissWithAnimation() },
                null, 0
            )
            else -> Unit
        }
    }

    private fun setupNetworking() {
        if (mConnectionReceiver == null) mConnectionReceiver = InternetManager.getInstance()
        val mNetworkStateFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(mConnectionReceiver, mNetworkStateFilter)
        InternetManager.addConnectionListener(this)
    }

    private fun teardownNetworking() {
        mConnectionReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (e: IllegalArgumentException) {
                Timber.w(e, "Connection receiver was not registered")
            }
            InternetManager.removeConnectionListener(this)
            mConnectionReceiver = null
        }
    }

    override fun onConnectionChanged(isConnected: Boolean) {
        val thisContext: Context = this@BrainwalletActivity
        val ltcStats = BRSharedPrefs.getLiveLtcStats(thisContext)
        val startHeight = ltcStats.currentBlockHeight
        if (isConnected) {
            BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(
                Runnable {
                    val progress = BRPeerManager.syncProgress(startHeight)
                    if (progress > 0 && progress < 1) {
                        SyncThreadManager.getInstance().startSyncing(startHeight)
                    }
                }
            )
        } else {
            SyncThreadManager.getInstance().stopSyncing()
        }
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
                        LEGACY_EFFECT_RESET_PIN,
                        address = null
                    )
                )
            }
        } else if (BRSharedPrefs.getPhraseWroteDown(this).not()) {
            PostAuth.getInstance().onPhraseCheckAuth(this, false)
            Timber.d("initWallet: post auth onPhraseCheckAuth")
        }
    }

    /**
     * provide old logic to use compose unlock screen instead of LoginActivity
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
    private fun onUnlock(passcode: List<Int>) {
        if (AuthManager.getInstance().checkAuth(passcode.joinToString(""), this)) {
            AuthManager.getInstance().authSuccess(this)

            AnalyticsManager.logCustomEvent(BWConstants._20200217_DU)
            LegacyNavigation.startBrainwalletActivity(this, false)
        } else {
            // Auth fail toast
            Toast.makeText(this, R.string.incorrect_passcode, Toast.LENGTH_SHORT).show()
        }
    }

    private fun onPasscodeVerified(passcode: List<Int>) {
        AuthManager.getInstance().authSuccess(this)
        AuthManager.getInstance().setPinCode(passcode.joinToString(separator = ""), this)
        if (intent.getBooleanExtra("noPin", false)) {
            LegacyNavigation.startBrainwalletActivity(this, false)
        } else {
            BRAnimator.showBreadSignal(
                this,
                getString(R.string.Alerts_pinSet),
                getString(R.string.UpdatePin_createInstruction),
                R.drawable.ic_check_mark_white
            ) {
                val walletNotAvailable = BRWalletManager.getInstance().noWallet(this)
                if (walletNotAvailable) {
                    PostAuth.getInstance().onCreateWalletAuth(this, false)
                } else {
                    PostAuth.getInstance().onPhraseCheckAuth(this, false)
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        appVisible = false
        removeObservers()
        teardownNetworking()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onRestart() {
        super.onRestart()
    }

    private fun addObservers() {
        BRWalletManager.getInstance().addBalanceChangedListener(this)
    }

    private fun removeObservers() {
        BRWalletManager.getInstance().removeListener(this)
    }

    override fun onResume() {
        super.onResume()
        appVisible = true
        addObservers()
        setupNetworking()

        if (!BRWalletManager.getInstance().isCreated()) {
            BRExecutor.getInstance().forBackgroundTasks().execute {
                BRWalletManager.getInstance().initWallet(this)
            }
        }
        BRWalletManager.getInstance().refreshBalance(this)
    }

    override fun onBalanceChanged(balance: Long) {
        Timber.d("timber: BrainwalletActivity subscribed onBalanceChanged $balance")
    }

    override fun onStatusPeerManagerUpdate() {
        Timber.d("timber: BrainwalletActivity subscribed onStatusUpdate")
    }

    override fun onTxAdded() {
        BRWalletManager.getInstance().refreshBalance(this)
        Timber.d("timber: BrainwalletActivity subscribed onTxAdded")
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
