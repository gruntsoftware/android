package com.brainwallet.presenter.activities

import android.os.Bundle
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.brainwallet.databinding.ActivitySetPasscodeBinding
import com.brainwallet.presenter.activities.util.BRActivity
import com.brainwallet.ui.screen.setpasscode.SetPasscodeMainScreen
import com.brainwallet.ui.screen.setpasscode.SetPasscodeReadyScreen
import com.brainwallet.ui.screen.setpasscode.SetPasscodeScreen
import com.brainwallet.ui.theme.setContentWithTheme


class SetPasscodeActivity: BRActivity() {

    private lateinit var binding: ActivitySetPasscodeBinding

    private val codeListSize = 6

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySetPasscodeBinding.inflate(layoutInflater)
        val view = binding.root
        val digits = listOf<Int>(codeListSize)

        setContentView(view)

        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContentWithTheme {

                SetPasscodeMainScreen(
                    onBackClick = {
                        finish()
                    },
                    digits = digits
                )
            }
        }
    }
        private fun onReadyClick() {

        }

        private fun onEnterPasscode() {

        }
}
