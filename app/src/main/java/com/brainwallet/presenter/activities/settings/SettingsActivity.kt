package com.brainwallet.presenter.activities.settings

import android.os.Bundle
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.brainwallet.databinding.ActivitySettingsBinding
import com.brainwallet.presenter.activities.util.BRActivity
import com.brainwallet.ui.theme.setContentWithTheme

class SettingsActivity : BRActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContentWithTheme {

            }
        }
    }
}
