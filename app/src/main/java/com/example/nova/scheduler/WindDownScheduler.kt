package com.example.nova.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.nova.blocker.AppBlockerService
import java.util.Calendar

object WindDownScheduler {
    fun scheduleAlarms(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 1. Setup the 11:00 PM Start Alarm
        val nightIntent = Intent(context, WindDownReceiver::class.java).apply {
            putExtra("START_BLOCKER", true)
        }
        val nightPending = PendingIntent.getBroadcast(
            context, 11, nightIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val nightCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23) // 11 PM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }

        // 2. Setup the 8:00 AM Stop Alarm
        val morningIntent = Intent(context, WindDownReceiver::class.java).apply {
            putExtra("START_BLOCKER", false)
        }
        val morningPending = PendingIntent.getBroadcast(
            context, 8, morningIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val morningCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8) // 8 AM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_YEAR, 1)
        }

        // 3. Schedule both alarms
        try {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nightCalendar.timeInMillis, nightPending)
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, morningCalendar.timeInMillis, morningPending)
        } catch (e: SecurityException) {
            // Handled safely if permission is missing
        }

        // 4. Instant Check: Immediately enforce if the app is launched during lock-out hours
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (currentHour >= 23 || currentHour < 8) {
            val serviceIntent = Intent(context, AppBlockerService::class.java).apply {
                putExtra("MODE", "NIGHT_MODE")
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }
}