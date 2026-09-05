package com.example.nova.ui

import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.nova.data.TaskCategory
import com.example.nova.data.TimeSlot
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTomorrowTaskDialog(
    tomorrowDate: LocalDate,
    onDismiss: () -> Unit,
    onSave: (TimeSlot) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(TaskCategory.OTHER) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var startHour by remember { mutableStateOf(9) }
    var startMinute by remember { mutableStateOf(0) }
    var endHour by remember { mutableStateOf(10) }
    var endMinute by remember { mutableStateOf(0) }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add task for tomorrow") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task title") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                Text("Category", style = MaterialTheme.typography.labelMedium)
                Box {
                    OutlinedButton(
                        onClick = { categoryExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(category.label)
                    }
                    DropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        TaskCategory.values().forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.label) },
                                onClick = { category = cat; categoryExpanded = false }
                            )
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))

                Row {
                    OutlinedButton(onClick = {
                        TimePickerDialog(
                            context,
                            { _, h, m -> startHour = h; startMinute = m },
                            startHour, startMinute, true
                        ).show()
                    }) { Text("Start %02d:%02d".format(startHour, startMinute)) }

                    Spacer(Modifier.width(8.dp))

                    OutlinedButton(onClick = {
                        TimePickerDialog(
                            context,
                            { _, h, m -> endHour = h; endMinute = m },
                            endHour, endMinute, true
                        ).show()
                    }) { Text("End %02d:%02d".format(endHour, endMinute)) }
                }

                if (showError) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Add a title first",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isBlank()) {
                    showError = true
                    return@TextButton
                }
                onSave(
                    TimeSlot(
                        title = title.trim(),
                        category = category,
                        startHour = startHour,
                        startMinute = startMinute,
                        endHour = endHour,
                        endMinute = endMinute,
                        isOneOff = true,
                        specificDate = tomorrowDate
                    )
                )
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
