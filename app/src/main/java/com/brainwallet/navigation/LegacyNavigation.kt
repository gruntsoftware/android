package com.brainwallet.navigation

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import com.brainwallet.BuildConfig
import com.brainwallet.R
import com.brainwallet.data.source.RemoteApiSource
import com.brainwallet.di.getKoinInstance
import com.brainwallet.presenter.activities.BreadActivity
import com.brainwallet.tools.util.Utils
import com.brainwallet.ui.BrainwalletActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    @JvmOverloads
    @JvmStatic
    fun showMoonPayWidget(
        context: Context,
        params: Map<String, String> = mapOf(),
        isDarkMode: Boolean = true,
    ) {
        val remoteApiSource: RemoteApiSource = getKoinInstance()
        val agentString = Utils.getAgentString(context, "android/HttpURLConnection")
        val progressDialog = ProgressDialog(context).apply {
            setMessage(context.getString(R.string.loading))
            setCancelable(false)
            show()
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    remoteApiSource.getMoonpaySignedUrl(
                        params = params.toMutableMap().apply {
                            put("defaultCurrencyCode", "ltc")
                            put("externalTransactionId", agentString)
                            put("currencyCode", "ltc")
                            put("themeId", "main-v1.0.0")
                            put("theme", if (isDarkMode) "dark" else "light")
                        }
                    )
                }

                val widgetUri = result.signedUrl.toUri().buildUpon()
                    .apply {
                        if (BuildConfig.DEBUG) {
                            authority("buy-sandbox.moonpay.com")//replace base url from buy.moonpay.com
                        }
                    }
                    .build()
                val intent = CustomTabsIntent.Builder()
                    .setColorScheme(if (isDarkMode) CustomTabsIntent.COLOR_SCHEME_DARK else CustomTabsIntent.COLOR_SCHEME_LIGHT)
                    .build()
                intent.launchUrl(context, widgetUri)
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Failed to load: ${e.message}, please try again later",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                progressDialog.dismiss()
            }
        }
    }

}