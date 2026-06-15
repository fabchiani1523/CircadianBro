package com.example.sleepcyclealarm.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationUtils {
    const val ALARM_CHANNEL_ID = "alarm_channel"

    fun createAlarmChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            ALARM_CHANNEL_ID,
            "Sveglia",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifiche usate quando la sveglia sta suonando."
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            enableVibration(true)
            setSound(null, null)
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}
