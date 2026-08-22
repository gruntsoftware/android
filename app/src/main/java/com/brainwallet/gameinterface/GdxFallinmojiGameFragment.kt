package com.brainwallet.gameinterface
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.badlogic.gdx.backends.android.AndroidFragmentApplication
import com.badlogic.gdx.backends.android.AndroidGraphics
import ltd.grunt.brainwallet.gamegdx.AndroidEmojiRenderer
import ltd.grunt.brainwallet.gamegdx.FallinmojiMainApplication
import ltd.grunt.brainwallet.gamegdx.interfaces.AudioSetup

class GdxFallinmojiGameFragment(
    var onExit: (String, ByteArray?) -> Unit = { _, _ -> },
) : AndroidFragmentApplication() {
    private val mainHandler = Handler(Looper.getMainLooper())

    private val emojiRenderer = AndroidEmojiRenderer()
    private var debugFeaturesEnabled: Boolean = false

    private val launchParams: String
        get() = arguments?.getString(ARG_LAUNCH_PARAMS).orEmpty()

    // Per https://developer.android.com/develop/ui/views/touch-and-input/gestures/gesturenav#conflicting-gestures:
    // the game reads swipes/taps near the screen edges as gameplay input, which otherwise
    // race against the system's edge-swipe gesture nav (back / predictive back). We're already
    // in sticky immersive mode (useImmersiveMode = true below), so excluding the whole view is
    // not subject to the usual 200dp-per-edge exclusion cap, and the game gets every touch while
    // it's the top view.
    private val gestureExclusionListener =
        View.OnLayoutChangeListener { view, _, _, _, _, _, _, _, _ -> applyGestureExclusion(view) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val config = AndroidApplicationConfiguration().apply {
            useImmersiveMode = true
            useGL30 = false
        }

        val view = initializeForView(
            FallinmojiMainApplication(
                {
                        jsonString, data ->
                    mainHandler.post { onExit(jsonString, data) }
                },
                launchParams,
                emojiRenderer,
                AudioSetup { miniAudio -> miniAudio.setupAndroid(requireContext().assets) },
                debugFeaturesEnabled
            ),
            config
        )
        view.addOnLayoutChangeListener(gestureExclusionListener)
        applyGestureExclusion(view)
        return view
    }

    override fun onDestroyView() {
        view?.let {
            it.removeOnLayoutChangeListener(gestureExclusionListener)
            it.systemGestureExclusionRects = emptyList()
        }
        super.onDestroyView()
    }

    /** Claims the whole view for gameplay touch so device edge-swipe gestures are ignored while playing. */
    private fun applyGestureExclusion(view: View) {
        if (view.width <= 0 || view.height <= 0) return
        view.systemGestureExclusionRects = listOf(Rect(0, 0, view.width, view.height))
    }

    override fun onPause() {
        val graphics = Gdx.graphics
        if (graphics is AndroidGraphics) {
            // Pump draw frames so AndroidGraphics.pause() can observe the pause
            // flag clear. lifecycleScope is dead during teardown, so use a plain
            // handler loop on the main thread instead.
            val deadline = System.currentTimeMillis() + 1000
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
