package com.example.nova.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeSlotDao {

    @Query("SELECT * FROM time_slots WHERE isActive = 1 ORDER BY startHour, startMinute")
    fun getAllActiveSlots(): Flow<List<TimeSlot>>

    @Query("SELECT * FROM time_slots WHERE isOneOff = 1 AND specificDate = :date AND isActive = 1")
    suspend fun getOneOffTasksForDate(date: String): List<TimeSlot>

    @Query("SELECT * FROM time_slots WHERE id = :id")
    suspend fun getById(id: Long): TimeSlot?

    @Insert
    suspend fun insert(slot: TimeSlot): Long

    @Update
    suspend fun update(slot: TimeSlot)

    @Delete
    suspend fun delete(slot: TimeSlot)
}
