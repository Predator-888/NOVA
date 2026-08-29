package com.example.nova.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.nova.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = AppDatabase.getInstance(context).timeSlotDao()
                val slots = dao.getAllActiveSlots().first()
                val scheduler = AlarmScheduler(context)
                slots.forEach { scheduler.scheduleSlot(it) }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
