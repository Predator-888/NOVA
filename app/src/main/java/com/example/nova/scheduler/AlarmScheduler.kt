package com.example.nova.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.nova.data.TimeSlot
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * Turns a [TimeSlot] into a scheduled system alarm that fires a notification
 * at the slot's start time. Recurring slots get rescheduled for their next
 * occurrence each time they fire (see AlarmReceiver).
 */
class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleSlot(slot: TimeSlot) {
        if (!slot.notificationsEnabled) return
        val triggerTime = nextTriggerTimeMillis(slot) ?: return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_SLOT_ID, slot.id)
            putExtra(EXTRA_TITLE, slot.title)
            putExtra(EXTRA_CATEGORY, slot.category.name)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            slot.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val canScheduleExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        if (canScheduleExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        } else {
            // Falls back to an inexact alarm if the user hasn't granted the
            // "alarms & reminders" permission yet (Android 12+).
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }

    fun cancelSlot(slot: TimeSlot) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            slot.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun nextTriggerTimeMillis(slot: TimeSlot): Long? {
        val now = ZonedDateTime.now()
        val zone = ZoneId.systemDefault()

        if (slot.isOneOff) {
            val date = slot.specificDate ?: return null
            val dt = ZonedDateTime.of(date, LocalTime.of(slot.startHour, slot.startMinute), zone)
            return if (dt.isAfter(now)) dt.toInstant().toEpochMilli() else null
        }

        if (slot.daysOfWeek.isEmpty()) return null

        val candidates = slot.daysOfWeek.map { day ->
            var dt = ZonedDateTime.of(now.toLocalDate(), LocalTime.of(slot.startHour, slot.startMinute), zone)
            while (dt.dayOfWeek != day || dt.isBefore(now)) {
                dt = dt.plusDays(1)
            }
            dt
        }
        return candidates.minByOrNull { it.toInstant().toEpochMilli() }?.toInstant()?.toEpochMilli()
    }

    companion object {
        const val EXTRA_SLOT_ID = "slot_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_CATEGORY = "category"
    }
}
