package com.example.nova.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ReadingRepository(
    private val bookDao: BookDao,
    private val logDao: ReadingLogDao
) {
    fun getCurrentBook(): Flow<Book?> = bookDao.getCurrentBook()

    suspend fun addBook(title: String, uriString: String): Long =
        bookDao.insert(Book(title = title, uriString = uriString))

    suspend fun updateProgress(book: Book, newPage: Int, totalPages: Int) {
        bookDao.update(book.copy(lastReadPage = newPage, totalPages = totalPages))
    }

    suspend fun removeBook(book: Book) = bookDao.delete(book)

    suspend fun logPageRead() {
        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val existing = logDao.getLogForDate(today)
        logDao.upsert(ReadingLog(date = today, pagesRead = (existing?.pagesRead ?: 0) + 1))
    }

    fun getAllLogs(): Flow<List<ReadingLog>> = logDao.getAllLogs()

    suspend fun getCurrentStreak(): Int {
        val logs = getAllLogs().first().associateBy { it.date }
        var streak = 0
        var day = LocalDate.now()
        // If today has no reading logged yet, start counting from yesterday
        // so an unbroken streak up to yesterday still displays correctly.
        val todayKey = day.format(DateTimeFormatter.ISO_LOCAL_DATE)
        if ((logs[todayKey]?.pagesRead ?: 0) <= 0) {
            day = day.minusDays(1)
        }
        while (true) {
            val key = day.format(DateTimeFormatter.ISO_LOCAL_DATE)
            val log = logs[key]
            if (log != null && log.pagesRead > 0) {
                streak++
                day = day.minusDays(1)
            } else {
                break
            }
        }
        return streak
    }
}
