package com.brainwallet.ui.screens.gamehub

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.lifecycle.viewModelScope
import com.brainwallet.appreview.InAppReviewService
import com.brainwallet.tools.manager.AnalyticsManager
import com.brainwallet.ui.BrainwalletViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.koin.android.annotation.KoinViewModel
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream

@KoinViewModel
class GameHubViewModel(
    private val app: Application,
    private val inAppReviewService: InAppReviewService,
) : BrainwalletViewModel<GameHubEvent>() {
    private val _state =
        MutableStateFlow(GameHubState())
    val state: StateFlow<GameHubState> = _state.asStateFlow()

    override fun onEvent(event: GameHubEvent) {
        when (event) {
            is GameHubEvent.OnLoad -> {
            }
            is GameHubEvent.OnGameFinished -> {
                AnalyticsManager.logCustomEventWithParams("did_play_game", null)
                viewModelScope.launch {
                    delay(3_000L)
                    inAppReviewService.showInAppReviewDialogIfNeeded()
                    Timber.d("did_request_rating");
                }
            }
            is GameHubEvent.OnGameExited -> {
                val unixTimestamp = System.currentTimeMillis() / 1000
                val bitmap = BitmapFactory.decodeByteArray(event.byteArray, 0, event.byteArray.size)
                    ?: throw error("Failed to decode image data")
                val directory = app.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    ?: throw error("External storage is unavailable")
                val file = File(directory, "$unixTimestamp.png")

                val jsonElement = Json.parseToJsonElement(event.jsonPayload)
                val socialNetworkString = jsonElement.jsonObject["social_network"]?.jsonPrimitive?.content ?: "other"

                FileOutputStream(file).use { fos ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                }

                val contentUri = FileProvider.getUriForFile(
                    app,
                    "${app.packageName}.fileprovider",
                    file
                )

                val shareText = "Look at my top score on Brainwallet!   #Brainwallet #Litecoin"
                val encodedText = java.net.URLEncoder.encode(shareText, "UTF-8")

                // Generic share intent
                val genericIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    putExtra(Intent.EXTRA_STREAM, contentUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                // X app intent
                val xAppIntent = Intent(genericIntent).apply {
                    setPackage("com.twitter.android")
                }

                // X web fallback (text only, no image)
                val xWebIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = android.net.Uri.parse("https://x.com/intent/tweet?text=$encodedText")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                // Instagram Stories intent (image only, shares as a sticker on a Story)
                val instagramIntent = Intent("com.instagram.share.ADD_TO_STORY").apply {
                    setDataAndType(contentUri, "image/png")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }

                val pm = app.packageManager
                val xAppAvailable = pm.resolveActivity(xAppIntent, 0) != null
                val xWebAvailable = pm.resolveActivity(xWebIntent, 0) != null
                val instagramAvailable = pm.resolveActivity(instagramIntent, 0) != null

                val params = Bundle()
                params.putString("social_network", socialNetworkString)
                AnalyticsManager.logCustomAdHocEvent("user_may_post_score_to_social", params)

                when (socialNetworkString) {
                    "twitter" -> when {
                        xAppAvailable -> app.startActivity(xAppIntent)
                        xWebAvailable -> app.startActivity(xWebIntent)
                        else -> app.startActivity(
                            Intent.createChooser(genericIntent, "Share Brainwallet Score").also {
                                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        )
                    }
                    "instagram" -> when {
                        instagramAvailable -> app.startActivity(instagramIntent)
                        else -> app.startActivity(
                            Intent.createChooser(genericIntent, "Share Brainwallet Score").also {
                                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                        )
                    }
                    else -> app.startActivity(
                        Intent.createChooser(genericIntent, "Share Brainwallet Score").also {
                            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                    )
                }
            }
        }
    }
}
