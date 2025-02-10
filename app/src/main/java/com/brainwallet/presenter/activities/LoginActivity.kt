package com.brainwallet.presenter.activities

import android.os.Bundle
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.brainwallet.databinding.ActivityPinBinding
import com.brainwallet.presenter.activities.util.BRActivity
import com.brainwallet.ui.screen.unlock.UnLockScreen
import com.brainwallet.ui.theme.setContentWithTheme

class LoginActivity : BRActivity() {

    private lateinit var binding: ActivityPinBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPinBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)


        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContentWithTheme {
                UnLockScreen(
                    onEvent = {
                        //todo
                    }
                )
            }
        }
    }
}
