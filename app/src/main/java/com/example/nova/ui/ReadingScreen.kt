package com.example.nova.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingScreen(viewModel: ReadingViewModel = viewModel()) {
    val book by viewModel.currentBook.collectAsState()
    val streak by viewModel.streak.collectAsState()
    var showReader by remember { mutableStateOf(false) }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.onBookPicked(it) }
    }

    val currentBook = book
    if (showReader && currentBook != null) {
        PdfReaderScreen(
            book = currentBook,
            onExit = { showReader = false },
            onPageRead = { newPage, total -> viewModel.onPageRead(currentBook, newPage, total) }
        )
        return
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Reading") }) }) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Streak: $streak day${if (streak == 1) "" else "s"}", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(24.dp))

            if (currentBook == null) {
                Text("No book uploaded yet.", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { pickerLauncher.launch(arrayOf("application/pdf")) }) {
                    Text("Upload a PDF")
                }
            } else {
                Text(currentBook.title, style = MaterialTheme.typography.titleLarge)
                if (currentBook.totalPages > 0) {
                    Text(
                        "Page ${currentBook.lastReadPage + 1} of ${currentBook.totalPages}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = { showReader = true }) { Text("Continue reading") }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { viewModel.removeBook(currentBook) }) { Text("Remove book") }
            }
        }
    }
}
