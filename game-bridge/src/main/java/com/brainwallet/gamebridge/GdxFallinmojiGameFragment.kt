package com.brainwallet.gamebridge
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.backends.android.AndroidFragmentApplication
import ltd.grunt.brainwallet.gamegdx.Main
import android.os.Handler
import android.os.Looper
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.android.AndroidGraphics

class GdxFallinmojiGameFragment (
    private val onExit: () -> Unit = {}
) : AndroidFragmentApplication() {
    private val mainHandler = Handler(Looper.getMainLooper())

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
            Main {
                // GL thread returns immediately; exit handled on a fresh UI message
                mainHandler.post { onExit() }
            },
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
}
