package com.example.nova.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * A single block on your timetable.
 *
 * Recurring blocks (college hours, study, meditation, exercise) set [isOneOff] = false
 * and list the [daysOfWeek] they repeat on.
 *
 * "Plan tomorrow" tasks you add at night are one-off: [isOneOff] = true with a
 * [specificDate] instead of recurring days.
 */
@Entity(tableName = "time_slots")
data class TimeSlot(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: TaskCategory,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val daysOfWeek: Set<DayOfWeek> = emptySet(),
    val isOneOff: Boolean = false,
    val specificDate: LocalDate? = null,
    val notificationsEnabled: Boolean = true,
    val isActive: Boolean = true
)
