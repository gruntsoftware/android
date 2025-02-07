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
import com.brainwallet.tools.security.SmartValidator
import com.brainwallet.tools.util.Utils
import com.brainwallet.ui.screen.inputwords.InputWordsEvent
import com.brainwallet.ui.screen.inputwords.InputWordsScreen
import com.brainwallet.ui.theme.setContentWithTheme
import com.brainwallet.wallet.BRWalletManager


class InputWordsActivity : BRActivity() {

    private lateinit var binding: ActivityInputWordsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityInputWordsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


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
        if (SmartValidator.isPaperKeyValid(app, cleanPhrase)) {

            if (SmartValidator.isPaperKeyCorrect(cleanPhrase, app)) {
                Utils.hideKeyboard(app)

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
                        val intent = Intent(
                            app, IntroActivity::class.java
                        )
                        finalizeIntent(intent)
                    },
                    { brDialogView -> brDialogView.dismissWithAnimation() },
                    null,
                    0
                )

            } else {
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
            }

        } else {
            BRDialog.showCustomDialog(
                app,
                "",
                resources.getString(R.string.RecoverWallet_invalid),
                getString(R.string.AccessibilityLabels_close),
                null,
                { brDialogView -> brDialogView.dismissWithAnimation() },
                null,
                null,
                0
            )
        }
    }

    private fun finalizeIntent(intent: Intent) {
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left)
        startActivity(intent)
        if (!this@InputWordsActivity.isDestroyed) finish()
        val app: Activity? = BreadActivity.getApp()
        if (app != null && !app.isDestroyed) app.finish()
    }
}
