package com.brainwallet.presenter.activities

import android.os.Bundle
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.brainwallet.databinding.ActivityInputWordsBinding
import com.brainwallet.presenter.activities.util.BRActivity
import com.brainwallet.ui.screen.inputwords.InputWordsEvent
import com.brainwallet.ui.screen.inputwords.InputWordsScreen
import com.brainwallet.ui.theme.setContentWithTheme

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
                InputWordsScreen(
                    onEvent = { event ->
                        when (event) {
                            InputWordsEvent.OnBackClick -> finish()
                            else -> Unit
                        }
                    }
                )
            }
        }
    }
}
