package com.brainwallet.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.brainwallet.R
import com.brainwallet.ui.BrainwalletActivity
import timber.log.Timber
object LegacyNavigation {

    /**
     * Wrapper for old `startBreadActivity`
     * Previously routed to BentoActivity via BentoNavigation — now routes directly
     * to BrainwalletActivity with Route.Main (auth=false) or Route.UnLock (auth=true).
     */
    @JvmStatic
    fun startBrainwalletActivity(
        from: Activity,
        auth: Boolean
    ) {
        Timber.i("timber: startBrainwalletActivity: %s", from.javaClass.name)

        val destination = if (auth) Route.UnLock() else Route.Main
        restartBrainwalletActivity(from, destination)
    }

    /**
     * Restarts BrainwalletActivity with an arbitrary start destination, clearing the back
     * stack - same clear-task/finish pattern as startBrainwalletActivity/restartBreadActivity,
     * just parameterized for callers that need to land somewhere other than Main/UnLock.
     */
    @JvmStatic
    fun restartBrainwalletActivity(from: Activity, destination: Route) {
        BrainwalletActivity.createIntent(from, destination).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }.also {
            from.startActivity(it)
        }

        from.overridePendingTransition(R.anim.fade_up, R.anim.fade_down)
        if (!from.isDestroyed) {
            from.finish()
        }
    }

    /**
     * Restarts the main wallet activity, clearing the back stack.
     * Previously pointed at the legacy BreadActivity.
     */
    @JvmStatic
    fun restartBreadActivity(context: Context) {
        BrainwalletActivity.createIntent(context, Route.Main).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        }.also {
            context.startActivity(it)
        }
    }

    /**
     * Opens a specific Compose screen from a legacy (non-Compose) context.
     */
    @JvmStatic
    @JvmOverloads
    fun openComposeScreen(
        context: Context,
        destination: Route = Route.Welcome
    ) = BrainwalletActivity.createIntent(context, destination).also {
        context.startActivity(it)
    }
}
