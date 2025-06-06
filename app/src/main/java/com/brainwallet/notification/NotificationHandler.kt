package com.brainwallet.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.brainwallet.R
import com.brainwallet.presenter.activities.BreadActivity
import com.google.firebase.messaging.RemoteMessage
import com.brainwallet.notification.NotificationHandler.NOTIFICATION_CHANNEL_ID_GENERAL
import com.brainwallet.notification.NotificationHandler.NOTIFICATION_CHANNEL_ID_LITECOIN_NEWS
import com.brainwallet.notification.NotificationHandler.NOTIFICATION_CHANNEL_ID_BRAINWALLET_UPDATE

object NotificationHandler {

    @SuppressLint("MissingPermission")
    fun handleMessageReceived(context: Context, remoteMessage: RemoteMessage): Boolean {
        if (remoteMessage.data.containsKey(KEY_DATA_BRAINWALLET).not()) {
            return false
        }

        val channelId = remoteMessage.notification?.channelId ?: NOTIFICATION_CHANNEL_ID_GENERAL
        val title = remoteMessage.data["title"] ?: return false
        val body = remoteMessage.data["body"] ?: return false

        if (defaultNotificationChannels.contains(channelId).not()) {
            createNotificationChannel(context, channelId, channelId)
        }

        val intent = Intent(context, BreadActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.brainwallet_logotype_white)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        NotificationManagerCompat.from(context)
            .notify(System.currentTimeMillis().toInt(), notification.build())

        return true
    }

    const val KEY_DATA_BRAINWALLET = "brainwallet"

    const val NOTIFICATION_CHANNEL_ID_GENERAL = "general"
    const val NOTIFICATION_CHANNEL_ID_LITECOIN_NEWS = "litecoin-news"
    const val NOTIFICATION_CHANNEL_ID_BRAINWALLET_UPDATE = "brainwallet-update"

    val defaultNotificationChannels = setOf(
        NOTIFICATION_CHANNEL_ID_GENERAL,
        NOTIFICATION_CHANNEL_ID_LITECOIN_NEWS,
        NOTIFICATION_CHANNEL_ID_BRAINWALLET_UPDATE
    )
}

fun setupNotificationChannels(context: Context) {
    createNotificationChannel(
        context,
        NOTIFICATION_CHANNEL_ID_GENERAL,
        context.getString(R.string.notification_channel_name_general)
    )
    createNotificationChannel(
        context,
        NOTIFICATION_CHANNEL_ID_LITECOIN_NEWS,
        context.getString(R.string.notification_channel_name_litecoin_news)
    )
    createNotificationChannel(
        context,
        NOTIFICATION_CHANNEL_ID_BRAINWALLET_UPDATE,
        context.getString(R.string.notification_channel_name_brainwallet_update)
    )
}

private fun createNotificationChannel(
    context: Context,
    channelId: String,
    name: String,
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        NotificationChannel(
            channelId,
            name,
            NotificationManager.IMPORTANCE_DEFAULT
        ).also { notificationChannel ->
            val notificationManager =
                context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}