package com.brainwallet.gamebridge
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.backends.android.AndroidFragmentApplication
import ltd.grunt.brainwallet.gamegdx.FallinmojiMainApplication
import android.os.Handler
import android.os.Looper
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.android.AndroidGraphics

class GdxFallinmojiGameFragment (
    var onExit: (ByteArray?) -> Unit = {},
) : AndroidFragmentApplication() {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val launchParams: String
        get() = arguments?.getString(ARG_LAUNCH_PARAMS).orEmpty()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val config = AndroidApplicationConfiguration().apply {
            useImmersiveMode = true
            useGL30 = false
        }

        return initializeForView(
            FallinmojiMainApplication(
                { data -> mainHandler.post { onExit(data) } },
                launchParams
            ),
            config
        )

    }

    override fun onPause() {
        val graphics = Gdx.graphics
        if (graphics is AndroidGraphics) {
            // Pump draw frames so AndroidGraphics.pause() can observe the pause
            // flag clear. lifecycleScope is dead during teardown, so use a plain
            // handler loop on the main thread instead.
            val deadline = System.currentTimeMillis() + 1500
            val pump = object : Runnable {
                override fun run() {
                    try {
                        graphics.onDrawFrame(null)
                    } catch (_: Throwable) {
                        return
                    }
                    if (System.currentTimeMillis() < deadline) {
                        mainHandler.post(this)
                    }
                }
            }
            mainHandler.post(pump)
        }
        super.onPause()
    }
    companion object {
        private const val ARG_LAUNCH_PARAMS = "launch_params"
        fun newInstance(launchParams: String) = GdxFallinmojiGameFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_LAUNCH_PARAMS, launchParams)
            }
        }
    }
}
