package com.brainwallet.presenter.activities

import android.os.Bundle
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.brainwallet.databinding.ActivityWelcomeBinding
import com.brainwallet.presenter.activities.util.BRActivity
import com.brainwallet.tools.security.PostAuth
import com.brainwallet.ui.screens.welcome.WelcomeEvent
import com.brainwallet.ui.screens.welcome.WelcomeScreen
import com.brainwallet.ui.screens.yourseedwords.YourSeedWordsScreen
import com.brainwallet.ui.screens.yourseedwords.YourSeedWordsEvent
import com.brainwallet.ui.theme.setContentWithTheme

class WelcomeActivity : BRActivity() {

    private lateinit var binding: ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContentWithTheme {
                WelcomeScreen(
                    onEvent = { action ->
                        when (action) {
                            WelcomeEvent.OnReadyClick -> onReadyClick()
                            WelcomeEvent.OnRestoreClick -> onRestoreClick()
                            WelcomeEvent.OnToggleTheme -> onToggleThemeClick()
                        }
                    }
                )
            }
        }
    }

    private fun onReadyClick() {
    }

    private fun onRestoreClick() {
    }

    private fun onToggleThemeClick() {
    }
}