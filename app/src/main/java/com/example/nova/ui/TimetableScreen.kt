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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(viewModel: TimetableViewModel = viewModel()) {
    val slots by viewModel.slots.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Your timetable") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add time block")
            }
        }
    ) { padding ->
        if (slots.isEmpty()) {
            Column(
                modifier = Modifier.padding(padding).fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No time blocks yet. Tap + to add your first one.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(slots, key = { it.id }) { slot ->
                    SlotRow(slot = slot, onDelete = { viewModel.deleteSlot(slot) })
                    Divider()
                }
            }
        }
    }

    if (showAddDialog) {
        AddSlotDialog(
            onDismiss = { showAddDialog = false },
            onSave = { slot ->
                viewModel.addSlot(slot)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun SlotRow(slot: TimeSlot, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(slot.title, style = MaterialTheme.typography.titleMedium)
            Text(
                "${slot.category.label} · " +
                    "%02d:%02d".format(slot.startHour, slot.startMinute) + " - " +
                    "%02d:%02d".format(slot.endHour, slot.endMinute),
                style = MaterialTheme.typography.bodySmall
            )
            if (!slot.isOneOff && slot.daysOfWeek.isNotEmpty()) {
                Text(
                    slot.daysOfWeek.sortedBy { it.value }.joinToString(", ") { it.name.take(3) },
                    style = MaterialTheme.typography.bodySmall
                )
            } else if (slot.isOneOff) {
                Text("Tomorrow only", style = MaterialTheme.typography.bodySmall)
            }
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete")
        }
    }
}
