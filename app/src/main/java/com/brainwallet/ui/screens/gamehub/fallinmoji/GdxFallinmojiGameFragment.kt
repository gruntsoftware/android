package com.brainwallet.ui.screens.gamehub.fallinmoji
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.grunt.brainwallet

import com.grunt.brainwallet.

class GdxFallinmojiGameFragment : AndroidFragmentApplication() {
        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            val config = AndroidApplicationConfiguration().apply {
                useImmersiveMode = true
                useGL30 = false        // match what your game targets
            }
            // Swap in your actual ApplicationListener / Game subclass:
            return initializeForView(Main(), config)
        }
    }
