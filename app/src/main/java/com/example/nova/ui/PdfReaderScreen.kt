package com.example.nova.ui

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.nova.data.Book
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfReaderScreen(
    book: Book,
    onExit: () -> Unit,
    onPageRead: (newPage: Int, totalPages: Int) -> Unit
) {
    val context = LocalContext.current
    var renderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var fileDescriptor by remember { mutableStateOf<ParcelFileDescriptor?>(null) }
    var pageIndex by remember { mutableStateOf(book.lastReadPage) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    DisposableEffect(book.uriString) {
        try {
            val uri = Uri.parse(book.uriString)
            val pfd = context.contentResolver.openFileDescriptor(uri, "r")
            fileDescriptor = pfd
            val newRenderer = pfd?.let { PdfRenderer(it) }
            renderer = newRenderer
            if (newRenderer != null && pageIndex >= newRenderer.pageCount) pageIndex = 0
        } catch (e: Exception) {
            errorMessage = "Couldn't open this file. It may have been moved or deleted."
        }
        onDispose {
            renderer?.close()
            fileDescriptor?.close()
        }
    }

    LaunchedEffect(renderer, pageIndex) {
        val r = renderer ?: return@LaunchedEffect
        if (pageIndex < 0 || pageIndex >= r.pageCount) return@LaunchedEffect
        val rendered = withContext(Dispatchers.Default) {
            val page = r.openPage(pageIndex)
            val bmp = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
            page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            bmp
        }
        bitmap = rendered
        onPageRead(pageIndex, r.pageCount)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(book.title, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onExit) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            val currentError = errorMessage
            val currentBitmap = bitmap
            val totalPages = renderer?.pageCount ?: 0

            Column(
                modifier = Modifier.weight(1f).fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when {
                    currentError != null -> Text(currentError)
                    currentBitmap != null -> Image(
                        bitmap = currentBitmap.asImageBitmap(),
                        contentDescription = "Page ${pageIndex + 1}"
                    )
                    else -> CircularProgressIndicator()
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (pageIndex > 0) pageIndex-- },
                    enabled = pageIndex > 0
                ) { Icon(Icons.Default.ChevronLeft, contentDescription = "Previous page") }

                Text("Page ${pageIndex + 1} of $totalPages")

                IconButton(
                    onClick = { if (pageIndex < totalPages - 1) pageIndex++ },
                    enabled = pageIndex < totalPages - 1
                ) { Icon(Icons.Default.ChevronRight, contentDescription = "Next page") }
            }
        }
    }
}
