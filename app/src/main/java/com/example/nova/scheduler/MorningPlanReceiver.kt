package com.example.nova.scheduler

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

class MorningPlanReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // 1. Read the saved priorities from memory
        val prefs = context.getSharedPreferences("NovaPrefs", Context.MODE_PRIVATE)
        val p1 = prefs.getString("priority_0", "") ?: ""
        val p2 = prefs.getString("priority_1", "") ?: ""
        val p3 = prefs.getString("priority_2", "") ?: ""

        val bigText = StringBuilder()
        if (p1.isNotBlank()) bigText.append("1. $p1\n")
        if (p2.isNotBlank()) bigText.append("2. $p2\n")
        if (p3.isNotBlank()) bigText.append("3. $p3")

        // 2. Set up the Notification Channel
        val channelId = "MorningPlanChannel"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Daily Planning Reminders",
                NotificationManager.IMPORTANCE_HIGH // High importance makes it pop up on screen
            )
            manager.createNotificationChannel(channel)
        }

        // 3. Build and fire the notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // Default alarm icon
            .setContentTitle("Today's Top 3 Priorities")
            .setContentText("Expand to see your plan")
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText.toString()))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(2001, notification)
    }
}