package com.brainwallet.appreview

import android.app.Activity
import android.app.Application
import com.brainwallet.data.repository.GameScoreRepository
import com.brainwallet.tools.manager.BRSharedPrefs
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import timber.log.Timber
import java.lang.Void

class InAppReviewService(
    private val app: Application,
    private val gameScoreRepository: GameScoreRepository,
    private val activityProvider: () -> Activity?,
) {
    // Review gate for the non-game call sites: never reviewed yet and 2+ successful sends, else no-op
    public fun showInAppReviewDialogIfNeeded() {
        if (BRSharedPrefs.getSendTransactionCount(app) > 1) {
            launchReviewIfNotDone()
        }
    }

    /**
     * Review gate for finished games: never reviewed yet and [GAMES_PLAYED_FOR_REVIEW]+ recorded
     * games, else no-op. Matches iOS `requestReviewIfEnoughGamesPlayed` (`gameScores.count >= 3`),
     * so unlike [showInAppReviewDialogIfNeeded] it doesn't need any sends.
     * Returns true when a review was requested.
     */
    public fun showInAppReviewDialogIfEnoughGamesPlayed(): Boolean {
        if (gameScoreRepository.gamesPlayedCount() < GAMES_PLAYED_FOR_REVIEW) return false
        return launchReviewIfNotDone()
    }

    private fun launchReviewIfNotDone(): Boolean {
        val activity = activityProvider() ?: return false
        if (BRSharedPrefs.isInAppReviewDone(app)) return false

        val manager = ReviewManagerFactory.create(app)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener(
            OnCompleteListener { task: Task<ReviewInfo>? ->
                if (task!!.isSuccessful()) {
                    val reviewInfo = task.getResult()
                    val flow = manager.launchReviewFlow(activity, reviewInfo)
                    flow.addOnCompleteListener(
                        OnCompleteListener { task1: Task<Void?>? ->
                            // The flow has finished. The API does not indicate whether the user
                            // reviewed or not, or even whether the review dialog was shown. Thus, no
                            // matter the result, we continue our app flow.
                            Timber.i(
                                "timber: In-app LaunchReviewFlow completed successful (%s)",
                                task1!!.isSuccessful()
                            )
                            if (task1.isSuccessful()) {
                                BRSharedPrefs.inAppReviewDone(app)
                            }
                        }
                    )
                } else {
                    Timber.e(task.getException(), "In-app request review flow failed")
                }
            }
        )
        return true
    }

    companion object {
        const val GAMES_PLAYED_FOR_REVIEW = 3
    }
}
