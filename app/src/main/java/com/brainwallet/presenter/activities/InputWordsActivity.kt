package com.brainwallet.presenter.activities


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.brainwallet.R
import com.brainwallet.databinding.ActivityInputWordsBinding
import com.brainwallet.presenter.activities.intro.IntroActivity
import com.brainwallet.presenter.activities.util.BRActivity
import com.brainwallet.tools.animation.BRAnimator
import com.brainwallet.tools.animation.BRDialog
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.tools.security.AuthManager
import com.brainwallet.tools.security.PostAuth
import com.brainwallet.tools.security.SmartValidator
import com.brainwallet.ui.screen.inputwords.InputWordsEvent
import com.brainwallet.ui.screen.inputwords.InputWordsScreen
import com.brainwallet.ui.theme.setContentWithTheme
import com.brainwallet.wallet.BRWalletManager


class InputWordsActivity : BRActivity() {

    private lateinit var binding: ActivityInputWordsBinding
    private var resetPin: Boolean = false
    private var restore: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInputWordsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        resetPin = intent.getBooleanExtra("resetPin", false)
        restore = intent.getBooleanExtra("restore", false)

        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContentWithTheme {
                InputWordsScreen(onEvent = { event ->
                    when (event) {
                        InputWordsEvent.OnBackClick -> finish()
                        is InputWordsEvent.OnRestoreClick -> onRestore(event.paperkey)
                        else -> Unit
                    }
                })
            }
        }
    }

    //TODO: revisit later, please move into new architecture
    private fun onRestore(paperkey: String) {
        if (!BRAnimator.isClickAllowed()) return
        val app: Activity = this@InputWordsActivity

        val cleanPhrase = SmartValidator.cleanPaperKey(app, paperkey)

        if (resetPin and SmartValidator.isPaperKeyCorrect(cleanPhrase, app).not()) {
            BRDialog.showCustomDialog(
                app,
                "",
                getString(R.string.RecoverWallet_invalid),
                getString(R.string.AccessibilityLabels_close),
                null,
                { brDialogView -> brDialogView.dismissWithAnimation() },
                null,
                null,
                0
            )
            return
        }


        if (resetPin) {
            AuthManager.getInstance().setPinCode("", this@InputWordsActivity)
            val intent = Intent(
                app,
                SetPinActivity::class.java
            )
            intent.putExtra("noPin", true)
            finalizeIntent(intent)
            return
        }

        if (restore) {
            BRDialog.showCustomDialog(
                this@InputWordsActivity,
                getString(R.string.WipeWallet_alertTitle),
                getString(R.string.WipeWallet_alertMessage),
                getString(R.string.WipeWallet_wipe),
                getString(R.string.Button_cancel),
                { brDialogView ->
                    brDialogView.dismissWithAnimation()
                    val m = BRWalletManager.getInstance()
                    m.wipeWalletButKeystore(app)
                    m.wipeKeyStore(app)
                    val intent = Intent(app, IntroActivity::class.java)
                    finalizeIntent(intent)
                },
                { brDialogView -> brDialogView.dismissWithAnimation() },
                null,
                0
            )
            return
        }


        val m = BRWalletManager.getInstance()
        m.wipeWalletButKeystore(app)
        m.wipeKeyStore(app)
        PostAuth.getInstance().setPhraseForKeyStore(cleanPhrase)
        BRSharedPrefs.putAllowSpend(app, false)

        //Did not upgrade to the new app, we installed it
        PostAuth.getInstance().onRecoverWalletAuth(app, false)
    }

    private fun finalizeIntent(intent: Intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        startActivity(intent)
        if (!this@InputWordsActivity.isDestroyed) finish()
        val app: Activity? = BreadActivity.getApp()
        if (app != null && !app.isDestroyed) app.finish()
    }
}
