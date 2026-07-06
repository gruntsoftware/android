package com.brainwallet.gameinterface

import android.view.View
import androidx.activity.compose.LocalActivity
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.badlogic.gdx.Gdx
import com.brainwallet.R
import com.brainwallet.ui.bentosections.gamehubbento.FallinScene
import com.brainwallet.ui.screens.gamehub.GameHubEvent
import com.brainwallet.ui.screens.gamehub.GameHubViewModel
import com.brainwallet.ui.screens.main.MainScreenEvent
import com.brainwallet.ui.screens.main.MainViewModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.compose.viewmodel.koinViewModel
import timber.log.Timber

@Serializable
private data class GameExitData(
    @SerialName("social_network") val socialNetwork: String = "none",
    val timestamp: Long = 0L,
    @SerialName("total_score") val totalScore: Int = 0,
    @SerialName("bonus_amount") val bonusAmount: Int = 0,
    @SerialName("score_a") val scoreA: Int = 0,
    @SerialName("score_b") val scoreB: Int = 0,
    @SerialName("score_c") val scoreC: Int = 0,
)

private val lenientJson = Json { ignoreUnknownKeys = true }

@Composable
fun GdxGameView(
    visible: Boolean,
    launchParams: String,
    onExit: (String, ByteArray?) -> Unit,
    modifier: Modifier = Modifier,
    gameHubViewModel: GameHubViewModel = koinViewModel(),
    viewModel: MainViewModel = koinViewModel(),
) {
    val activity = LocalActivity.current as FragmentActivity
    val fm = activity.supportFragmentManager
    val containerId = remember { View.generateViewId() }
    val tag = remember { "gdx_fallinmoji_$containerId" }
    val currentOnExit by rememberUpdatedState(onExit)
    val currentLaunchParams by rememberUpdatedState(launchParams)

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            FragmentContainerView(ctx).apply { id = containerId }
        },
        update = { _ ->
            if (visible) {
                if (fm.findFragmentByTag(tag) == null && !fm.isStateSaved) {
                    val fragment = GdxFallinmojiGameFragment
                        .newInstance(currentLaunchParams)
                        .apply {
                            this.onExit = { jsonString, bytes ->
                                removeFragment(fm, tag)
                                viewModel.onEvent(MainScreenEvent.OnToggleGameHub)
                                handleGameExit(jsonString, bytes, gameHubViewModel)
                                currentOnExit(jsonString, bytes)
                            }
                        }
                    fm.commit { add(containerId, fragment, tag) }
                }
                try {
                    Gdx.app?.postRunnable { Gdx.graphics?.requestRendering() }
                } catch (_: Throwable) { }
            }
        }
    )

    DisposableEffect(Unit) {
        onDispose { removeFragment(fm, tag) }
    }
}

private fun removeFragment(fm: FragmentManager, tag: String) {
    val frag = fm.findFragmentByTag(tag)
    if (frag != null && !fm.isStateSaved) {
        fm.commit(allowStateLoss = true) { remove(frag) }
    }
}

private fun handleGameExit(
    jsonString: String,
    bytes: ByteArray?,
    gameHubViewModel: GameHubViewModel,
) {
    val exitData = try {
        lenientJson.decodeFromString<GameExitData>(jsonString)
    } catch (e: Exception) {
        Timber.w(e, "Failed to parse game exit JSON: %s", jsonString)
        return
    }

    Timber.d(
        "Game exit: social=%s score=%d bonus=%d",
        exitData.socialNetwork,
        exitData.totalScore,
        exitData.bonusAmount
    )

    when (exitData.socialNetwork) {
        "twitter", "instagram" -> {
            val screenshot = bytes?.takeIf { it.isNotEmpty() } ?: run {
                Timber.w("Social share requested but no screenshot data")
                return
            }
            gameHubViewModel.onEvent(
                GameHubEvent.OnGameExited(
                    jsonPayload = jsonString,
                    byteArray = screenshot
                )
            )
        }

        else -> Timber.d("Game completed, returning to wallet")
    }
}

@Composable
fun GameSplash(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.game_hub_bk),
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
