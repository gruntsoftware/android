package com.brainwallet.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.brainwallet.R
import com.brainwallet.presenter.activities.BreadActivity
import com.brainwallet.tools.manager.BRSharedPrefs
import com.brainwallet.ui.BrainwalletActivity
import timber.log.Timber
import androidx.core.net.toUri


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

    @JvmStatic
    fun showMoonPayWidget(context: Context) {
        //todo: wip here
        val apiKey = ""
        val address = BRSharedPrefs.getReceiveAddress(context)

        val baseUri = "https://buy.moonpay.com/v2/buy".toUri()
            .buildUpon()
            .appendQueryParameter("apiKey", apiKey)
            .appendQueryParameter("defaultCurrencyCode", "ltc")
            .appendQueryParameter("baseCurrencyCode", "usd")
//            .appendQueryParameter("baseCurrencyAmount", "84")
//            .appendQueryParameter("walletAddress", address)
//            .appendQueryParameter("colorCode", "")
            .appendQueryParameter("theme", "dark")
            .appendQueryParameter("themeId", "main-v1.0.0")
            .appendQueryParameter("language", "en")
            .appendQueryParameter("quoteCurrencyAmount", "84")
//            .appendQueryParameter("externalTransactionId", "")
//            .appendQueryParameter("redirectURL", "")
//            .appendQueryParameter("skipUnsupportedRegionScreen", "")
            .build()

        val intent = CustomTabsIntent.Builder()
            .build()
        intent.launchUrl(context, baseUri)
    }

}