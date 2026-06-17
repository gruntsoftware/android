package com.brainwallet.gamebridge

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.compose.runtime.getValue
import androidx.fragment.app.commit
import com.badlogic.gdx.Gdx

@Composable
fun GdxGameView(
    modifier: Modifier,
    visible: Boolean,
    launchParams: String,
    onExit: (ByteArray?) -> Unit
) {
    val activity = LocalContext.current as FragmentActivity
    val fm = activity.supportFragmentManager
    val containerId = remember { View.generateViewId() }
    val tag = remember { "gdx_fallinmoji_$containerId" }
    val currentOnExit by rememberUpdatedState(onExit)

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            FragmentContainerView(ctx).apply {
                id = containerId
                post {
                    if (fm.findFragmentByTag(tag) == null && !fm.isStateSaved) {
                        val fragment = GdxFallinmojiGameFragment
                            .newInstance(launchParams)
                            .apply { this.onExit = { data -> currentOnExit(data) } }
                        fm.commit { add(containerId, fragment, tag) }
                    }
                }
            }
        },
        update = { container ->
            container.visibility = if (visible) View.VISIBLE else View.GONE
            // pause/resume libGDX rendering with visibility
            try {
                if (visible) Gdx.app?.postRunnable { Gdx.graphics?.requestRendering() }
            } catch (_: Throwable) { }
        }
    )
    DisposableEffect(Unit) {
        onDispose {
            val frag = fm.findFragmentByTag(tag)
            if (frag != null && !fm.isStateSaved) {
                fm.commit(allowStateLoss = true) {
                    remove(frag)
                }
            }
        }
    }
}
