package com.brainwallet.ui.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.brainwallet.R
import com.brainwallet.data.model.AppSetting
import com.brainwallet.data.repository.SettingRepository
import com.brainwallet.presenter.activities.util.BRActivity
import com.brainwallet.tools.animation.BRAnimator
import com.brainwallet.tools.animation.BRDialog
import com.brainwallet.tools.security.AuthManager
import com.brainwallet.tools.security.PostAuth
import com.brainwallet.ui.screens.reenterpin.ReEnterPinIntent
import com.brainwallet.ui.screens.reenterpin.ReEnterPinScreen
import com.brainwallet.ui.screens.reenterpin.ReEnterPinSideEffect
import com.brainwallet.ui.screens.reenterpin.ReEnterPinViewModel
import com.brainwallet.ui.theme.BrainwalletAppTheme
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import timber.log.Timber

class ReEnterPinActivity : BRActivity() {

    private val settingRepository by inject<SettingRepository>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup modern back press handling
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    finish()
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                        overrideActivityTransition(
                            OVERRIDE_TRANSITION_CLOSE,
                            R.anim.enter_from_left,
                            R.anim.exit_to_right
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
                    }
                }
            }
        )

        val firstPin = intent.extras?.getString(EXTRA_PIN)

        setContent {
            val appSetting by settingRepository.settings.collectAsState(AppSetting())
            val viewModel = koinViewModel<ReEnterPinViewModel>()
            val state by viewModel.collectAsState()

            // Initialize ViewModel with the first PIN
            if (state.originalPin.isEmpty()) {
                viewModel.handleIntent(ReEnterPinIntent.Initialize(firstPin))
            }

            // Handle side effects
            viewModel.collectSideEffect { sideEffect ->
                when (sideEffect) {
                    ReEnterPinSideEffect.NavigateBack -> {
                        Timber.d("NavigateBack side effect received")
                        finish()
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                            overrideActivityTransition(
                                OVERRIDE_TRANSITION_CLOSE,
                                R.anim.enter_from_left,
                                R.anim.exit_to_right
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right)
                        }
                    }

                    ReEnterPinSideEffect.NavigateToSuccess -> {
                        Timber.d("NavigateToSuccess side effect received")
                        handlePinSuccess(state.currentPin)
                    }

                    is ReEnterPinSideEffect.ShowError -> {
                        Timber.e("ShowError side effect: ${sideEffect.message}")
                        showErrorDialog(sideEffect.message)
                    }

                    ReEnterPinSideEffect.TriggerHapticFeedback -> {
                        triggerHapticFeedback()
                    }

                    ReEnterPinSideEffect.PlayErrorAnimation -> {
                        // Animation is handled in the Compose UI
                        Timber.d("PlayErrorAnimation side effect received")
                    }
                }
            }

            BrainwalletAppTheme(appSetting = appSetting) {
                ReEnterPinScreen(
                    state = state,
                    onIntent = viewModel::handleIntent
                )
            }
        }
    }

    private fun handlePinSuccess(pin: String) {
        lifecycleScope.launch {
            try {
                AuthManager.getInstance().authSuccess(this@ReEnterPinActivity)
                AuthManager.getInstance().setPinCode(pin, this@ReEnterPinActivity)

                val noPin = intent.getBooleanExtra(EXTRA_NO_PIN, false)

                if (noPin) {
                    BRAnimator.startBreadActivity(this@ReEnterPinActivity, false)
                } else {
                    BRAnimator.showBreadSignal(
                        this@ReEnterPinActivity,
                        getString(R.string.Alerts_pinSet),
                        getString(R.string.UpdatePin_createInstruction),
                        R.drawable.ic_check_mark_white
                    ) {
                        PostAuth.getInstance().onCreateWalletAuth(this@ReEnterPinActivity, false)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error handling PIN success")
                showErrorDialog("An error occurred while setting your PIN")
            }
        }
    }

    private fun showErrorDialog(message: String) {
        BRDialog.showCustomDialog(
            this,
            getString(R.string.JailbreakWarnings_title),
            message,
            getString(R.string.AccessibilityLabels_close),
            null,
            { brDialogView ->
                brDialogView.dismissWithAnimation()
                finish()
            },
            null,
            null,
            0
        )
    }

    private fun triggerHapticFeedback() {
        try {
            val vibrator = getSystemService(VIBRATOR_SERVICE) as? Vibrator
            vibrator?.vibrate(
                VibrationEffect.createOneShot(
                    100,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
            )
        } catch (e: Exception) {
            Timber.w(e, "Failed to trigger haptic feedback")
        }
    }

    companion object {
        private const val EXTRA_PIN = "pin"
        private const val EXTRA_NO_PIN = "noPin"

        fun createIntent(context: Context, firstPin: String, noPin: Boolean = false): Intent {
            return Intent(context, ReEnterPinActivity::class.java).apply {
                putExtra(EXTRA_PIN, firstPin)
                putExtra(EXTRA_NO_PIN, noPin)
            }
        }
    }
}
