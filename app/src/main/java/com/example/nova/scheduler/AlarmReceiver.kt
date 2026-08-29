package com.example.nova.scheduler

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.nova.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val slotId = intent.getLongExtra(AlarmScheduler.EXTRA_SLOT_ID, -1)
        val title = intent.getStringExtra(AlarmScheduler.EXTRA_TITLE) ?: "Time block"
        val category = intent.getStringExtra(AlarmScheduler.EXTRA_CATEGORY) ?: "OTHER"

        NotificationHelper(context).showSlotNotification(slotId, title, category)

        // Recurring slots need to be rescheduled for their next occurrence
        // since exact alarms are one-shot.
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dao = AppDatabase.getInstance(context).timeSlotDao()
                val slot = dao.getById(slotId)
                if (slot != null && !slot.isOneOff) {
                    AlarmScheduler(context).scheduleSlot(slot)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
