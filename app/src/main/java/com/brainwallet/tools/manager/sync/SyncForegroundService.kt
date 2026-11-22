package com.brainwallet.tools.manager.sync

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.net.ConnectivityManager
import android.net.Network
import android.os.IBinder
import android.os.PowerManager
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.brainwallet.R
import com.brainwallet.data.source.PeerManagerSource
import com.brainwallet.ui.BrainwalletActivity
import com.brainwallet.wallet.BRPeerManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber

/**
 * Keeps the process and its peer sockets running in the background during a wallet sync, and
 * reconnects when the default network changes (the activity's connectivity receiver is only
 * registered while it is resumed).
 *
 * Only starts below [KEEP_ALIVE_BELOW], then stays until the sync is complete or
 * [STALL_TIMEOUT_MS] passes without a new block. Stopping it never stops the sync itself.
 */
class SyncForegroundService : Service() {

    private val peerManagerSource: PeerManagerSource by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var wakeLock: PowerManager.WakeLock? = null
    private var lastWakeLockAt = 0L
    private var observing = false
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                buildNotification(null),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } catch (e: Exception) {
            Timber.w(e, "timber: SyncForegroundService: startForeground failed")
            stopSelf()
            return START_NOT_STICKY
        }

        if (!observing) {
            observing = true
            observeNetwork()
            observeProgress()
            observeStall()
            observeFailures()
        }
        return START_NOT_STICKY
    }

    private fun observeProgress() {
        scope.launch {
            peerManagerSource.blockInfo
                .filter { it.syncProgress > 0f } // 0 means no sync is running: keep showing the last percent
                .map { percentToShow(it.syncProgress) }
                .distinctUntilChanged()
                .collect { percent ->
                    if (percent == null) {
                        Timber.d("timber: SyncForegroundService: sync complete, stopping")
                        stopSelf()
                    } else {
                        Timber.d("timber: SyncForegroundService: %d%%", percent)
                        notificationManager().notify(NOTIFICATION_ID, buildNotification(percent))
                    }
                }
        }
    }

    /** Covers a lost network, a failed sync and a wiped wallet: no new block means nothing to keep alive. */
    private fun observeStall() {
        scope.launch {
            peerManagerSource.blockInfo
                .map { it.blockHeight }
                .distinctUntilChanged()
                .collectLatest {
                    renewWakeLock()
                    delay(STALL_TIMEOUT_MS)
                    Timber.w("timber: SyncForegroundService: no new block, stopping")
                    stopSelf()
                }
        }
    }

    private fun observeFailures() {
        scope.launch {
            reconnectWhileIdle(SyncThreadManager.getInstance().syncState) {
                Timber.d("timber: SyncForegroundService: sync stopped, reconnecting")
                BRPeerManager.getInstance().networkChanged(true)
            }
        }
    }

    private fun observeNetwork() {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Timber.d("timber: SyncForegroundService: network available")
                BRPeerManager.getInstance().networkChanged(true)
            }
        }
        connectivityManager().registerDefaultNetworkCallback(callback)
        networkCallback = callback
    }

    // Android 15+: dataSync services get a daily time budget; give the slot back when it runs out.
    override fun onTimeout(startId: Int, fgsType: Int) {
        Timber.w("timber: SyncForegroundService: foreground time limit reached")
        stopSelf()
    }

    override fun onDestroy() {
        scope.cancel()
        networkCallback?.let { connectivityManager().unregisterNetworkCallback(it) }
        networkCallback = null
        wakeLock?.takeIf { it.isHeld }?.release()
        wakeLock = null
        super.onDestroy()
    }

    private fun renewWakeLock() {
        val now = SystemClock.elapsedRealtime()
        if (wakeLock != null && now - lastWakeLockAt < WAKE_LOCK_RENEW_MS) return
        lastWakeLockAt = now
        val lock = wakeLock ?: (getSystemService(Context.POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG)
            .apply { setReferenceCounted(false) }
            .also { wakeLock = it }
        lock.acquire(STALL_TIMEOUT_MS + WAKE_LOCK_RENEW_MS)
    }

    private fun buildNotification(percent: Int?): android.app.Notification {
        ensureChannel(this)
        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent(this),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.brainwallet_logotype_white)
            .setContentTitle(getString(R.string.sync_notification_title))
            .setContentText(percent?.let { getString(R.string.sync_notification_progress, it) })
            .setProgress(100, percent ?: 0, percent == null)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentIntent(contentIntent)
            .build()
    }

    /** Opens the app like its launcher icon does; SINGLE_TOP returns to the screen already on top. */
    private fun openAppIntent(context: Context): Intent =
        Intent.makeMainActivity(ComponentName(context, BrainwalletActivity::class.java))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)

    private fun connectivityManager() =
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private fun notificationManager() =
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        /** The service is only started while sync progress is below this. */
        const val KEEP_ALIVE_BELOW = 0.99f

        private const val NOTIFICATION_ID = 0x5B1C
        private const val CHANNEL_ID = "blockchain-sync"
        private const val WAKE_LOCK_TAG = "brainwallet:sync"
        private const val WAKE_LOCK_RENEW_MS = 60_000L
        private const val STALL_TIMEOUT_MS = 10 * 60_000L
        internal const val RETRY_MS = 15_000L

        /** The peer manager stops for good after too many failed peers, so connect again until a sync starts. */
        internal suspend fun reconnectWhileIdle(
            syncState: Flow<SyncThreadManager.SyncState>,
            reconnect: () -> Unit,
        ) {
            syncState
                .map { it is SyncThreadManager.SyncState.Idle }
                .distinctUntilChanged()
                .collectLatest { idle ->
                    while (idle) {
                        delay(RETRY_MS)
                        reconnect()
                    }
                }
        }

        fun needsKeepAlive(progress: Float): Boolean = progress < KEEP_ALIVE_BELOW

        /** Whole percent for the notification, or null once the sync is complete. */
        internal fun percentToShow(progress: Float): Int? =
            if (progress < 1f) (progress * 100).toInt() else null

        /**
         * Starts the service if [progress] still needs it. Has to be called while the app is in the foreground: Android 12+
         * refuses to start a foreground service from the background, which is logged and ignored.
         */
        @JvmStatic
        fun startIfNeeded(context: Context, progress: Float) {
            if (!needsKeepAlive(progress)) return
            try {
                ContextCompat.startForegroundService(
                    context.applicationContext,
                    Intent(context.applicationContext, SyncForegroundService::class.java)
                )
            } catch (e: Exception) {
                Timber.w(e, "timber: SyncForegroundService: could not start")
            }
        }

        private fun ensureChannel(context: Context) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (manager.getNotificationChannel(CHANNEL_ID) != null) return
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.sync_notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW
                ).apply { setShowBadge(false) }
            )
        }
    }
}
