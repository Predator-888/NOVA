package com.example.nova.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nova.data.AppDatabase
import com.example.nova.data.TimeSlot
import com.example.nova.data.TimetableRepository
import com.example.nova.scheduler.AlarmScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class TomorrowViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TimetableRepository(AppDatabase.getInstance(application).timeSlotDao())
    private val scheduler = AlarmScheduler(application)

    val tomorrowDate: LocalDate = LocalDate.now().plusDays(1)

    val tasks = repository.getTomorrowTasksFlow(tomorrowDate)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addTask(slot: TimeSlot) {
        viewModelScope.launch {
            val id = repository.addSlot(slot)
            scheduler.scheduleSlot(slot.copy(id = id))
        }
    }

    fun deleteTask(slot: TimeSlot) {
        viewModelScope.launch {
            scheduler.cancelSlot(slot)
            repository.deleteSlot(slot)
        }
    }
}
