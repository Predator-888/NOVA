package com.example.nova.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadingLogDao {

    @Query("SELECT * FROM reading_logs WHERE date = :date")
    suspend fun getLogForDate(date: String): ReadingLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: ReadingLog)

    @Query("SELECT * FROM reading_logs ORDER BY date DESC")
    fun getAllLogs(): Flow<List<ReadingLog>>
}
