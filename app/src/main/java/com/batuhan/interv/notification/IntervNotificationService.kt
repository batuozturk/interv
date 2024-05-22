package com.batuhan.interv.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import com.batuhan.interv.MainActivity
import com.batuhan.interv.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class IntervNotificationService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        if (message.data.isNotEmpty()) {
            val pendingIntent =
                PendingIntent.getActivity(
                    this,
                    100,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT,
                )
            val notificationBuilder =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Notification.Builder(this, "interv-notification-channel")
                } else {
                    Notification.Builder(this)
                }
            notificationBuilder.setContentText(message.notification?.body)
                .setContentTitle(message.notification?.title).setSmallIcon(R.drawable.ic_logo)
                .setContentIntent(pendingIntent)

            val notificationManager= getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(1, notificationBuilder.build())

        }
    }
}
