package com.brainwallet

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.os.Build
import com.appsflyer.AppsFlyerLib
import com.brainwallet.notification.NotificationHandler
import com.brainwallet.presenter.activities.util.BRActivity
import com.brainwallet.presenter.entities.ServiceItems
import com.brainwallet.tools.listeners.SyncReceiver
import com.brainwallet.tools.manager.AnalyticsManager
import com.brainwallet.tools.util.Utils
import com.brainwallet.constants.BWConstants
import com.getkeepsafe.relinker.ReLinker
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.koin.android.ext.android.inject
import timber.log.Timber
import timber.log.Timber.DebugTree
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.atomic.AtomicInteger

open class BrainwalletApp : Application() {

    private val notificationHandler: NotificationHandler by inject()

    override fun onCreate() {
        super.onCreate()
        loadNativeLibrary()
        val enableCrashlytics = !Utils.isEmulatorOrDebug(this)
        notificationHandler.setupNotificationChannels(this)

        AnalyticsManager.init(this)
        if (BuildConfig.DEBUG) {
            FirebaseCrashlytics.getInstance().setCustomKey("build_type", "debug")
            FirebaseCrashlytics.getInstance().setUserId("debug_bw_devices")
        }

        if (BuildConfig.DEBUG) Timber.plant(DebugTree())

        //  DEV: uncomment for debugging FirebaseMessaging
//                FirebaseMessaging.getInstance()
//                        .getToken()
//                        .addOnSuccessListener(new OnSuccessListener<String>() {
//                            @Override
//                            public void onSuccess(String s) {
//                                Timber.d("timber: fcm getToken= %s", s);
//                            }
//                        })
//                        .addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                Timber.d(e, "timber: fcm getToken failure");
//                            }
//                        });
        DISPLAY_HEIGHT_PX = Resources.getSystem().displayMetrics.heightPixels

        if (!BuildConfig.DEBUG) {
            val afID = Utils.fetchServiceItem(this, ServiceItems.AFDEVID)
            val appsFlyerLib = AppsFlyerLib.getInstance()
            appsFlyerLib.init(afID, null, this)
            appsFlyerLib.setCollectAndroidID(true)
            appsFlyerLib.start(this)
        }
    }

    /**
     * Loads the native wallet-crypto library (secp256k1 etc.).
     *
     * Tries the standard system loader first - it's the most reliable path on modern
     * Android and sidesteps some known ReLinker/APK-extraction quirks on API 31+ - then
     * falls back to ReLinker (kept for older/buggy OEM loaders it was originally added
     * to work around). If both fail, the wallet cannot perform any crypto operations, so
     * we record rich diagnostics (installer source + supported ABIs) as a non-fatal before
     * rethrowing, since a totally missing native lib is otherwise indistinguishable from a
     * tampered/repackaged APK installed outside the Play Store.
     */
    private fun loadNativeLibrary() {
        try {
            System.loadLibrary(BWConstants.NATIVE_LIB_NAME)
        } catch (systemLoadError: UnsatisfiedLinkError) {
            Timber.w(systemLoadError, "timber: System.loadLibrary failed, falling back to ReLinker")
            try {
                ReLinker.loadLibrary(this, BWConstants.NATIVE_LIB_NAME)
            } catch (reLinkerError: Throwable) {
                @Suppress("DEPRECATION")
                val installerPackageName = try {
                    packageManager.getInstallerPackageName(packageName)
                } catch (e: IllegalArgumentException) {
                    null
                }
                FirebaseCrashlytics.getInstance().apply {
                    setCustomKey("native_lib_installer_package", installerPackageName ?: "unknown")
                    setCustomKey("native_lib_supported_abis", Build.SUPPORTED_ABIS.joinToString())
                    recordException(reLinkerError)
                }
                throw reLinkerError
            }
        }
    }

    interface OnAppBackgrounded {
        fun onBackgrounded()
    }

    companion object {
        @JvmField
        var DISPLAY_HEIGHT_PX: Int = 0

        @JvmField
        var HOST: String = "apigsltd.net"
        private var listeners: MutableList<OnAppBackgrounded>? = null
        private var isBackgroundChecker: Timer? = null

        @JvmField
        var activityCounter: AtomicInteger = AtomicInteger()

        @JvmField
        var backgroundedTime: Long = 0

        @SuppressLint("StaticFieldLeak")
        private var currentActivity: Activity? = null

        @JvmStatic
        val breadContext: Context
            get() = if (currentActivity == null) SyncReceiver.app else currentActivity!!

        @JvmStatic
        fun setBreadContext(app: Activity?) {
            currentActivity = app
        }

        fun fireListeners() {
            if (listeners == null) return
            for (lis in listeners!!) lis.onBackgrounded()
        }

        fun addOnBackgroundedListener(listener: OnAppBackgrounded) {
            if (listeners == null) listeners = ArrayList()
            if (!listeners!!.contains(listener)) listeners!!.add(listener)
        }

        @JvmStatic
        fun isAppInBackground(context: Context?): Boolean {
            return context == null || activityCounter.get() <= 0
        }

        // call onStop on evert activity so
        @JvmStatic
        fun onStop(app: BRActivity?) {
            if (isBackgroundChecker != null) isBackgroundChecker!!.cancel()
            isBackgroundChecker = Timer()
            val backgroundCheck: TimerTask = object : TimerTask() {
                override fun run() {
                    if (isAppInBackground(app)) {
                        backgroundedTime = System.currentTimeMillis()
                        Timber.d("timber: App went in background!")
                        // APP in background, do something
                        isBackgroundChecker!!.cancel()
                        fireListeners()
                    }
                }
            }

            isBackgroundChecker!!.schedule(backgroundCheck, 500, 500)
        }
    }
}
