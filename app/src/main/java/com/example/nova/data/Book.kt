package com.example.nova.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class Book(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val uriString: String,
    val totalPages: Int = 0,
    val lastReadPage: Int = 0,
    val addedAtMillis: Long = System.currentTimeMillis()
)
