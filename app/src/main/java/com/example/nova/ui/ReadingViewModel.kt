package com.example.nova.ui

import android.app.Application
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nova.data.AppDatabase
import com.example.nova.data.Book
import com.example.nova.data.ReadingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReadingViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ReadingRepository(
        AppDatabase.getInstance(application).bookDao(),
        AppDatabase.getInstance(application).readingLogDao()
    )
    private val resolver: ContentResolver = application.contentResolver

    val currentBook = repository.getCurrentBook()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak

    init {
        refreshStreak()
    }

    fun refreshStreak() {
        viewModelScope.launch {
            _streak.value = repository.getCurrentStreak()
        }
    }

    fun onBookPicked(uri: Uri) {
        try {
            resolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        } catch (e: SecurityException) {
            // Some providers don't support persistable permissions; the book
            // will still work for this app session.
        }
        val name = queryDisplayName(uri) ?: "My book"
        viewModelScope.launch {
            repository.addBook(title = name, uriString = uri.toString())
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        return resolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
        }
    }

    fun removeBook(book: Book) {
        viewModelScope.launch { repository.removeBook(book) }
    }

    fun onPageRead(book: Book, newPage: Int, totalPages: Int) {
        viewModelScope.launch {
            repository.updateProgress(book, newPage, totalPages)
            repository.logPageRead()
            refreshStreak()
        }
    }
}
