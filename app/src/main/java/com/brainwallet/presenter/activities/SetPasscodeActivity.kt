package com.brainwallet.presenter.activities

import android.os.Bundle
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.brainwallet.databinding.ActivitySetPasscodeBinding
import com.brainwallet.presenter.activities.util.BRActivity
import com.brainwallet.tools.animation.BRAnimator
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.ui.screen.setpasscode.SetPasscodeScreen
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
                SetPasscodeScreen()
            }
        }
    }
}
