package com.brainwallet.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.brainwallet.R
import com.brainwallet.presenter.activities.BreadActivity
import com.brainwallet.ui.BrainwalletActivity
import timber.log.Timber


//provide old navigation using intent activity
object LegacyNavigation {

    //todo

    /**
     * wrapper for old `startBreadActivity`
     * at [com.brainwallet.tools.animation.BRAnimator.startBreadActivity]
     */
    @JvmStatic
    fun startBreadActivity(
        from: Activity,
        auth: Boolean
    ) {
        Timber.i("timber: startBreadActivity: %s", from.javaClass.name)
        val intent = if (auth) BrainwalletActivity.createIntent(from, Route.UnLock())
        else Intent(from, BreadActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        from.startActivity(intent)
        from.overridePendingTransition(R.anim.fade_up, R.anim.fade_down)
        if (!from.isDestroyed) {
            from.finish()
        }
    }

    @JvmStatic
    fun restartBreadActivity(
        context: Context
    ) {
        Intent(context, BreadActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }.also {
            context.startActivity(it)
        }
    }

    //open compose from old activity
    @JvmStatic
    @JvmOverloads
    fun openComposeScreen(
        context: Context,
        destination: Route = Route.Welcome
    ) = BrainwalletActivity.createIntent(context, destination).also {
        context.startActivity(it)
    }

}