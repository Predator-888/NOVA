package com.example.nova.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.nova.blocker.AppBlockerService

class WindDownReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val isStarting = intent.getBooleanExtra("START_BLOCKER", false)
        val serviceIntent = Intent(context, AppBlockerService::class.java)

        if (isStarting) {
            // Tell the blocker to use the Night Mode UI
            serviceIntent.putExtra("MODE", "NIGHT_MODE")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        } else {
            // Wake up time - turn the blocker off
            context.stopService(serviceIntent)
        }

        // Reschedule the alarms for the next day
        WindDownScheduler.scheduleAlarms(context)
    }
}