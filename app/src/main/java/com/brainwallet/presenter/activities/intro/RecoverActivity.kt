package com.brainwallet.presenter.activities.intro

import android.os.Bundle
import android.view.View
import com.brainwallet.databinding.ActivityIntroRecoverBinding
import com.brainwallet.navigation.LegacyNavigation
import com.brainwallet.navigation.Route
import com.brainwallet.presenter.activities.util.BRActivity
import com.brainwallet.tools.animation.BRAnimator

@Deprecated(message = "not used, migrate to compose")
class RecoverActivity : BRActivity() {

    private lateinit var binding: ActivityIntroRecoverBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityIntroRecoverBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        binding.sendButton.setOnClickListener(View.OnClickListener {
            if (!BRAnimator.isClickAllowed()) return@OnClickListener
            LegacyNavigation.openComposeScreen(
                context = this@RecoverActivity,
                destination = Route.InputWords()
            )
        })
    }

    override fun onResume() {
        super.onResume()
        appVisible = true
        app = this
    }

    override fun onPause() {
        super.onPause()
        appVisible = false
    }

    companion object {
        var appVisible: Boolean = false
        var app: RecoverActivity? = null
            private set
    }
}
