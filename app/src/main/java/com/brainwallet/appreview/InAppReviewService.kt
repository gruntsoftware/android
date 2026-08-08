package com.brainwallet.appreview

import android.app.Activity
import android.app.Application
import com.brainwallet.tools.manager.BRSharedPrefs
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import timber.log.Timber
import java.lang.Void

class InAppReviewService(
    private val app: Application,
    private val activityProvider: () -> Activity?,

) {
    public fun showInAppReviewDialogIfNeeded() {
        val activity = activityProvider() ?: return
        if (!BRSharedPrefs.isInAppReviewDone(app) &&
            BRSharedPrefs.getSendTransactionCount(app) > 1
        ) {
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
        }
    }
}
