package com.brainwallet.presenter.activities

import android.os.Bundle
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.brainwallet.databinding.ActivitySetPasscodeBinding
import com.brainwallet.presenter.activities.util.BRActivity
import com.brainwallet.ui.screen.setpasscode.SetPasscodeReadyScreen
import com.brainwallet.ui.screen.setpasscode.SetPasscodeReadyScreenEvent
import com.brainwallet.ui.screen.setpasscode.SetPasscodeScreen
import com.brainwallet.ui.screen.setpasscode.SetPasscodeScreenEvent
import com.brainwallet.ui.theme.setContentWithTheme


class SetPasscodeActivity: BRActivity() {

    private lateinit var binding: ActivitySetPasscodeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySetPasscodeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContentWithTheme {
                SetPasscodeReadyScreen(
                    onEvent = { action ->
                        when (action) {
                            SetPasscodeReadyScreenEvent.OnBackClick -> finish()
                            SetPasscodeReadyScreenEvent.OnReadyClick -> onReadyClick()
                        }
                    }
                )
                SetPasscodeScreen(
                    onEvent = { action ->
                        when (action) {
                            SetPasscodeScreenEvent.OnBackClick -> finish()
                            SetPasscodeScreenEvent.OnEnterPasscode -> onEnterPasscode()
                            SetPasscodeScreenEvent.OnClear -> TODO()
                            is SetPasscodeScreenEvent.OnLoad -> TODO()
                            is SetPasscodeScreenEvent.OnSetPasscode -> TODO()
                        }
                    },
                    digits = TODO(),
                    viewModel = TODO()
                )

            }
        }
    }
        private fun onReadyClick() {

        }

        private fun onEnterPasscode() {

        }
}
