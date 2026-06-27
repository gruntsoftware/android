package com.brainwallet.gamebridge

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import com.badlogic.gdx.Gdx

@Composable
fun GdxGameView(
    modifier: Modifier,
    visible: Boolean,
    launchParams: String,
    onExit: (String, ByteArray?) -> Unit
) {
    val activity = LocalContext.current as FragmentActivity
    val fm = activity.supportFragmentManager
    val containerId = remember { View.generateViewId() }
    val tag = remember { "gdx_fallinmoji_$containerId" }
    val currentOnExit by rememberUpdatedState(onExit)
    val currentLaunchParams by rememberUpdatedState(launchParams)

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            // Just create the container — fragment lifecycle is managed in update
            FragmentContainerView(ctx).apply {
                id = containerId
            }
        },
        update = { container ->
            if (visible) {
                container.visibility = View.VISIBLE
                // Re-add fragment if it was removed on a previous exit
                if (fm.findFragmentByTag(tag) == null && !fm.isStateSaved) {
                    val fragment = GdxFallinmojiGameFragment
                        .newInstance(currentLaunchParams)
                        .apply {
                            this.onExit = { jsonString, data ->
                                val frag = fm.findFragmentByTag(tag)
                                if (frag != null && !fm.isStateSaved) {
                                    fm.commit(allowStateLoss = true) { remove(frag) }
                                }
                                currentOnExit(jsonString, data)
                            }
                        }
                    fm.commit { add(containerId, fragment, tag) }
                }
                try {
                    Gdx.app?.postRunnable { Gdx.graphics?.requestRendering() }
                } catch (_: Throwable) { }
            } else {
                container.visibility = View.GONE
            }
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            val frag = fm.findFragmentByTag(tag)
            if (frag != null && !fm.isStateSaved) {
                fm.commit(allowStateLoss = true) { remove(frag) }
            }
        }
    }
}
