package com.example.nova.data

import kotlinx.coroutines.flow.Flow

class StudySessionRepository(private val dao: StudySessionDao) {

    suspend fun logSession(durationMinutes: Int, completed: Boolean) {
        dao.insert(
            StudySession(
                startedAtMillis = System.currentTimeMillis(),
                durationMinutes = durationMinutes,
                completed = completed
            )
        )
    }

    fun getCompletedCountSince(sinceMillis: Long): Flow<Int> = dao.getCompletedCountSince(sinceMillis)

    fun getTotalMinutesSince(sinceMillis: Long): Flow<Int> = dao.getTotalMinutesSince(sinceMillis)
}
