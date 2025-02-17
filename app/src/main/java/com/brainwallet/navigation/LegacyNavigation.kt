package com.brainwallet.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.brainwallet.R
import com.brainwallet.presenter.activities.BreadActivity
import com.brainwallet.presenter.activities.LoginActivity
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
        val toStart: Class<*> = if (auth) LoginActivity::class.java else BreadActivity::class.java
        val intent = Intent(from, toStart)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        from.startActivity(intent)
        from.overridePendingTransition(R.anim.fade_up, R.anim.fade_down)
        if (!from.isDestroyed) {
            from.finish()
        }
    }

    //open compose from old activity
    @JvmStatic
    fun openComposeScreen(
        context: Context,
        destination: Route = Route.Welcome
    ) = BrainwalletActivity.createIntent(context, destination).also {
        context.startActivity(it)
    }

}