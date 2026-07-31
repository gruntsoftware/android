package com.brainwallet.appreview

import android.app.Activity
import android.app.Application
import com.brainwallet.constants.BWConstants
import com.brainwallet.tools.manager.AnalyticsManager
import com.brainwallet.tools.manager.BRSharedPrefs
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.lang.Void

class InAppReviewServiceTest {

    private lateinit var app: Application
    private lateinit var activity: Activity
    private lateinit var manager: ReviewManager
    private lateinit var reviewInfo: ReviewInfo
    private lateinit var requestTask: Task<ReviewInfo>
    private lateinit var launchTask: Task<Void?>

    private fun service(activityResult: Activity? = activity) =
        InAppReviewService(app) { activityResult }

    @Before
    fun setUp() {
        app = mockk(relaxed = true)
        activity = mockk(relaxed = true)
        manager = mockk()
        reviewInfo = mockk()
        requestTask = mockk()
        launchTask = mockk()

        mockkStatic(ReviewManagerFactory::class)
        every { ReviewManagerFactory.create(app) } returns manager
        every { manager.requestReviewFlow() } returns requestTask
        every { manager.launchReviewFlow(activity, reviewInfo) } returns launchTask

        // Resolve both Play Core tasks synchronously so the flow under test
        // completes deterministically, without needing real Play Store services.
        every { requestTask.addOnCompleteListener(any()) } answers {
            firstArg<OnCompleteListener<ReviewInfo>>().onComplete(requestTask)
            requestTask
        }
        every { launchTask.addOnCompleteListener(any()) } answers {
            firstArg<OnCompleteListener<Void?>>().onComplete(launchTask)
            launchTask
        }

        mockkStatic(BRSharedPrefs::class)
        mockkStatic(AnalyticsManager::class)
        every { AnalyticsManager.logCustomEvent(any()) } returns Unit
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `given no current activity, when showInAppReviewDialogIfNeeded, then does not request a review`() {
        every { BRSharedPrefs.isInAppReviewDone(app) } returns false
        every { BRSharedPrefs.getSendTransactionCount(app) } returns 5

        service(activityResult = null).showInAppReviewDialogIfNeeded()

        verify(exactly = 0) { ReviewManagerFactory.create(any()) }
    }

    @Test
    fun `given review already done, when showInAppReviewDialogIfNeeded, then does not request a review`() {
        every { BRSharedPrefs.isInAppReviewDone(app) } returns true
        every { BRSharedPrefs.getSendTransactionCount(app) } returns 10

        service().showInAppReviewDialogIfNeeded()

        verify(exactly = 0) { ReviewManagerFactory.create(any()) }
    }

    @Test
    fun `given send count at threshold, when showInAppReviewDialogIfNeeded, then does not request a review`() {
        every { BRSharedPrefs.isInAppReviewDone(app) } returns false
        every { BRSharedPrefs.getSendTransactionCount(app) } returns 1

        service().showInAppReviewDialogIfNeeded()

        verify(exactly = 0) { ReviewManagerFactory.create(any()) }
    }

    @Test
    fun `given send count below threshold, when showInAppReviewDialogIfNeeded, then does not request a review`() {
        every { BRSharedPrefs.isInAppReviewDone(app) } returns false
        every { BRSharedPrefs.getSendTransactionCount(app) } returns 0

        service().showInAppReviewDialogIfNeeded()

        verify(exactly = 0) { ReviewManagerFactory.create(any()) }
    }

    @Test
    fun `given eligible user and successful review flow, when showInAppReviewDialogIfNeeded, then marks review done and logs both analytics events`() {
        every { BRSharedPrefs.isInAppReviewDone(app) } returns false
        every { BRSharedPrefs.getSendTransactionCount(app) } returns 5
        every { requestTask.isSuccessful() } returns true
        every { requestTask.getResult() } returns reviewInfo
        every { launchTask.isSuccessful() } returns true
        every { BRSharedPrefs.inAppReviewDone(app) } returns Unit

        service().showInAppReviewDialogIfNeeded()

        verify { manager.launchReviewFlow(activity, reviewInfo) }
        verify { BRSharedPrefs.inAppReviewDone(app) }
        verify { AnalyticsManager.logCustomEvent(BWConstants._20241006_DRR) }
        verify { AnalyticsManager.logCustomEvent(BWConstants._20241006_UCR) }
    }

    @Test
    fun `given request review flow fails, when showInAppReviewDialogIfNeeded, then does not launch review flow or mark done`() {
        every { BRSharedPrefs.isInAppReviewDone(app) } returns false
        every { BRSharedPrefs.getSendTransactionCount(app) } returns 5
        every { requestTask.isSuccessful() } returns false
        every { requestTask.getException() } returns Exception("boom")

        service().showInAppReviewDialogIfNeeded()

        verify(exactly = 0) { manager.launchReviewFlow(any(), any()) }
        verify(exactly = 0) { BRSharedPrefs.inAppReviewDone(app) }
        verify { AnalyticsManager.logCustomEvent(BWConstants._20241006_DRR) }
        verify(exactly = 0) { AnalyticsManager.logCustomEvent(BWConstants._20241006_UCR) }
    }

    @Test
    fun `given launch review flow fails, when showInAppReviewDialogIfNeeded, then does not mark done or log completion event`() {
        every { BRSharedPrefs.isInAppReviewDone(app) } returns false
        every { BRSharedPrefs.getSendTransactionCount(app) } returns 5
        every { requestTask.isSuccessful() } returns true
        every { requestTask.getResult() } returns reviewInfo
        every { launchTask.isSuccessful() } returns false

        service().showInAppReviewDialogIfNeeded()

        verify { manager.launchReviewFlow(activity, reviewInfo) }
        verify(exactly = 0) { BRSharedPrefs.inAppReviewDone(app) }
        verify { AnalyticsManager.logCustomEvent(BWConstants._20241006_DRR) }
        verify(exactly = 0) { AnalyticsManager.logCustomEvent(BWConstants._20241006_UCR) }
    }
}
