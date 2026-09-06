package com.example.nova.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.example.nova.scheduler.MorningPlanReceiver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Calendar

class PlanningViewModel : ViewModel() {

    private val _topPriorities = MutableStateFlow(listOf("", "", ""))
    val topPriorities: StateFlow<List<String>> = _topPriorities.asStateFlow()

    fun updatePriority(index: Int, text: String) {
        _topPriorities.update { currentList ->
            val newList = currentList.toMutableList()
            newList[index] = text
            newList
        }
    }

    fun lockInTomorrow(context: Context) {
        val priorities = _topPriorities.value

        // 1. Save priorities persistently to SharedPreferences
        val prefs = context.getSharedPreferences("NovaPrefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("priority_0", priorities[0])
            putString("priority_1", priorities[1])
            putString("priority_2", priorities[2])
            apply()
        }

        // 2. Schedule the alarm for 7:00 AM tomorrow
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, MorningPlanReceiver::class.java)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Set time to 7:00 AM tomorrow
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1) // Tomorrow
            set(Calendar.HOUR_OF_DAY, 7)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        // Use exact alarm (requires the exact alarm permission we already added)
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // Fallback if exact alarm permission was revoked
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }
}