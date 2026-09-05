package com.example.nova.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.nova.data.TimeSlot
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TomorrowScreen(viewModel: TomorrowViewModel = viewModel()) {
    val tasks by viewModel.tasks.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val dateLabel = viewModel.tomorrowDate.format(DateTimeFormatter.ofPattern("EEEE, MMM d"))

    Scaffold(
        topBar = { TopAppBar(title = { Text("Plan tomorrow · $dateLabel") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add task")
            }
        }
    ) { padding ->
        if (tasks.isEmpty()) {
            Column(
                modifier = Modifier.padding(padding).fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Nothing planned yet. Add tomorrow's tasks tonight so you wake up with a plan.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(tasks, key = { it.id }) { task ->
                    TomorrowTaskRow(task = task, onDelete = { viewModel.deleteTask(task) })
                    Divider()
                }
            }
        }
    }

    if (showAddDialog) {
        AddTomorrowTaskDialog(
            tomorrowDate = viewModel.tomorrowDate,
            onDismiss = { showAddDialog = false },
            onSave = { task ->
                viewModel.addTask(task)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun TomorrowTaskRow(task: TimeSlot, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(task.title, style = MaterialTheme.typography.titleMedium)
            Text(
                "${task.category.label} · " +
                    "%02d:%02d".format(task.startHour, task.startMinute) + " - " +
                    "%02d:%02d".format(task.endHour, task.endMinute),
                style = MaterialTheme.typography.bodySmall
            )
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete")
        }
    }
}
