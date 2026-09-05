package com.example.nova.data

import androidx.room.Entity

@Entity(tableName = "reading_logs", primaryKeys = ["date"])
data class ReadingLog(
    val date: String, // ISO yyyy-MM-dd
    val pagesRead: Int
)
