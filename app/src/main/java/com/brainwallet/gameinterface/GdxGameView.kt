package com.brainwallet.gameinterface

import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commit
import com.badlogic.gdx.Gdx
import com.brainwallet.R
import com.brainwallet.ui.bentosections.gamehubbento.FallinScene
import com.brainwallet.ui.screens.gamehub.GameHubEvent
import com.brainwallet.ui.screens.gamehub.GameHubViewModel
import com.brainwallet.ui.screens.main.MainScreenEvent
import com.brainwallet.ui.screens.main.MainViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun GdxGameView(
    visible: Boolean,
    launchParams: String,
    onExit: (String, ByteArray?) -> Unit,
    modifier: Modifier = Modifier,
    gameHubViewModel: GameHubViewModel = koinViewModel(),
    viewModel: MainViewModel = koinViewModel(),
) {
    val context = LocalContext.current
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
                            this.onExit = { jsonString, bytes ->
                                val frag = fm.findFragmentByTag(tag)
                                if (frag != null && !fm.isStateSaved) {
                                    fm.commit(allowStateLoss = true) { remove(frag) }
                                }
                                viewModel.onEvent(MainScreenEvent.OnToggleGameHub)
                                print(":::gameslot openGameSlot: $jsonString ${bytes?.size}")
                                if (jsonString.contains("NO_GAME_DATA") &&
                                    bytes?.isEmpty() == true
                                ) {
                                    print(":::gameslot No Game")
                                } else if ((
                                    jsonString.contains("twitter") ||
                                        jsonString.contains("instagram")
                                    )
                                ) {
                                    bytes?.takeIf { it.isNotEmpty() }?.let { byteArray ->
                                        gameHubViewModel
                                            .onEvent(
                                                GameHubEvent.OnGameExited(
                                                    jsonPayload = jsonString,
                                                    byteArray = byteArray
                                                )
                                            )
                                    }
                                }

                                currentOnExit(jsonString, bytes)
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

@Composable
fun GameSplash(modifier: Modifier = Modifier) {
    val gameHubBk = R.drawable.game_hub_bk
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(gameHubBk),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        FallinScene(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .align(Alignment.CenterEnd),
            dotQuantity = 24
        )
    }
}
