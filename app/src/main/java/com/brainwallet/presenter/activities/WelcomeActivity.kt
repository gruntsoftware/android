package com.brainwallet.presenter.activities

import android.os.Bundle
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.brainwallet.databinding.ActivityWelcomeBinding
import com.brainwallet.presenter.activities.util.BRActivity
import com.brainwallet.tools.security.PostAuth
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

        val phrase = intent.getStringExtra("phrase")
        if (phrase == null || phrase[phrase.length - 1] == '\u0000') {
            finish()
            return
        }
        val seedWords = phrase.split(" ")

        if (seedWords.size != 12) {
            finish()
            return
        }

        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContentWithTheme {
                YourSeedWordsScreen(
                    seedWords = seedWords,
                    onEvent = { action ->
                        when (action) {
                            YourSeedWordsEvent.OnBackClick -> finish()
                            YourSeedWordsEvent.OnSavedItClick -> onSavedItClick()
                        }
                    }
                )
            }
        }
    }

    private fun onSavedItClick() {
        PostAuth.getInstance().onPhraseProveAuth(this, false)
    }
}