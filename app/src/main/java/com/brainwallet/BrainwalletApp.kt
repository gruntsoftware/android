package com.brainwallet

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Resources
import com.appsflyer.AppsFlyerLib
import com.brainwallet.di.dataModule
import com.brainwallet.di.viewModelModule
import com.brainwallet.notification.setupNotificationChannels
import com.brainwallet.presenter.activities.util.BRActivity
import com.brainwallet.presenter.entities.ServiceItems
import com.brainwallet.tools.listeners.SyncReceiver
import com.brainwallet.tools.manager.AnalyticsManager
import com.brainwallet.tools.util.BRConstants
import com.brainwallet.tools.util.Utils
import com.brainwallet.util.cryptography.KeyStoreKeyGenerator
import com.brainwallet.util.cryptography.KeyStoreManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import org.koin.core.logger.Level
import timber.log.Timber
import timber.log.Timber.DebugTree
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.atomic.AtomicInteger

class BrainwalletApp : Application() {

    override fun onCreate() {
        super.onCreate()

        initializeModule()

        /** DEV:  Top placement requirement. */
        val enableCrashlytics = !Utils.isEmulatorOrDebug(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(enableCrashlytics)

        setupNotificationChannels(this)

        AnalyticsManager.init(this)
        AnalyticsManager.logCustomEvent(BRConstants._20191105_AL)

        if (BuildConfig.DEBUG) Timber.plant(DebugTree())

        //  DEV: uncomment for debugging
        //        FirebaseMessaging.getInstance()
        //                .getToken()
        //                .addOnSuccessListener(new OnSuccessListener<String>() {
        //                    @Override
        //                    public void onSuccess(String s) {
        //                        Timber.d("timber: fcm getToken= %s", s);
        //                    }
        //                })
        //                .addOnFailureListener(new OnFailureListener() {
        //                    @Override
        //                    public void onFailure(@NonNull Exception e) {
        //                        Timber.d(e, "timber: fcm getToken failure");
        //                    }
        //                });
        if (BuildConfig.DEBUG) Timber.plant(DebugTree())
        DISPLAY_HEIGHT_PX = Resources.getSystem().displayMetrics.heightPixels

        val afID = Utils.fetchServiceItem(this, ServiceItems.AFDEVID)
        val appsFlyerLib = AppsFlyerLib.getInstance()
        appsFlyerLib.init(afID, null, this)
        appsFlyerLib.setDebugLog(BuildConfig.DEBUG)
        appsFlyerLib.setCollectAndroidID(true)
        appsFlyerLib.start(this)
    }

    protected fun initializeModule() {
        keyStoreManager = KeyStoreManager(this, KeyStoreKeyGenerator.Impl())

        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR)
            androidContext(this@BrainwalletApp)
            modules(dataModule, viewModelModule)
        }
    }

//    override fun attachBaseContext(base: Context) {
//        init(base)
//        super.attachBaseContext(instance.setLocale(base))
//    }

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

        @SuppressLint("StaticFieldLeak")
        @JvmField
        var keyStoreManager: KeyStoreManager? = null

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

        //call onStop on evert activity so
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
