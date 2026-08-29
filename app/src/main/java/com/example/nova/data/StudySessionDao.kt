package com.example.nova.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StudySessionDao {

    @Insert
    suspend fun insert(session: StudySession)

    @Query("SELECT * FROM study_sessions ORDER BY startedAtMillis DESC")
    fun getAllSessions(): Flow<List<StudySession>>

    @Query("SELECT COUNT(*) FROM study_sessions WHERE completed = 1 AND startedAtMillis >= :sinceMillis")
    fun getCompletedCountSince(sinceMillis: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(durationMinutes), 0) FROM study_sessions WHERE completed = 1 AND startedAtMillis >= :sinceMillis")
    fun getTotalMinutesSince(sinceMillis: Long): Flow<Int>
}
