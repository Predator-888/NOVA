package com.example.nova.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class TimetableRepository(private val dao: TimeSlotDao) {

    fun getAllSlots(): Flow<List<TimeSlot>> = dao.getAllActiveSlots()

    suspend fun addSlot(slot: TimeSlot): Long = dao.insert(slot)

    suspend fun updateSlot(slot: TimeSlot) = dao.update(slot)

    suspend fun deleteSlot(slot: TimeSlot) = dao.delete(slot)

    suspend fun getTomorrowTasks(date: LocalDate): List<TimeSlot> =
        dao.getOneOffTasksForDate(date.toString())
}
