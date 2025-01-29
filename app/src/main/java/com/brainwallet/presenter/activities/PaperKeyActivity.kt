package com.brainwallet.presenter.activities

import android.os.Bundle
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.brainwallet.databinding.ActivityPaperKeyBinding
import com.brainwallet.presenter.activities.util.BRActivity
import com.brainwallet.ui.screen.yourseedwords.YourSeedWordsScreen
import com.brainwallet.ui.screen.yourseedwords.YourSeedWordsScreenAction
import com.brainwallet.ui.theme.BrainwalletAppTheme

class PaperKeyActivity : BRActivity() {

    private lateinit var binding: ActivityPaperKeyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPaperKeyBinding.inflate(layoutInflater)
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
            setContent {
                BrainwalletAppTheme {
                    YourSeedWordsScreen(
                        seedWords = seedWords,
                        onAction = { action ->
                            when (action) {
                                YourSeedWordsScreenAction.OnBackClick -> finish()
                                YourSeedWordsScreenAction.OnSavedItClick -> onSavedItClick()
                            }
                        }
                    )
                }
            }
        }
    }

    private fun onSavedItClick() {
        //todo
    }
}