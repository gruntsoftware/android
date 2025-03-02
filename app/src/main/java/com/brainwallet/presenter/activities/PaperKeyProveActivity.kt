package com.brainwallet.presenter.activities

import android.os.Bundle
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.brainwallet.databinding.ActivityPaperKeyProveBinding
import com.brainwallet.presenter.activities.util.BRActivity
import com.brainwallet.ui.theme.setContentWithTheme

@Deprecated(message = "move to compose")
class PaperKeyProveActivity : BRActivity() {

    private lateinit var binding: ActivityPaperKeyProveBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPaperKeyProveBinding.inflate(layoutInflater)
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
//                YourSeedProveItScreen(
//                    seedWords = seedWords,
//                    onEvent = { event ->
//                        when (event) {
//                            YourSeedProveItEvent.OnBackClick -> finish()
//                            YourSeedProveItEvent.OnGameAndSync -> {
//                                BRSharedPrefs.putPhraseWroteDown(this@PaperKeyProveActivity, true)
//                                BRAnimator.startBreadActivity(
//                                    this@PaperKeyProveActivity,
//                                    false
//                                )
//                                finishAffinity()
//                            }
//
//                            else -> Unit
//                        }
//                    }
//                )
            }
        }
    }
}
