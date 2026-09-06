package com.example.nova.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// 1. The Data Model (Removed the hardcoded icon requirement to make user-creation easier)
data class Habit(
    val id: Int,
    val title: String,
    val subtitle: String,
    val isCompleted: Boolean = false
)

// 2. The ViewModel
class HabitViewModel : ViewModel() {

    // Generic defaults that apply to anyone
    private val _habits = MutableStateFlow(
        listOf(
            Habit(1, "Deep Focus", "Complete 2 Pomodoro sessions"),
            Habit(2, "Physical Activity", "30 minutes of exercise"),
            Habit(3, "Digital Sunset", "No screens 1 hour before bed")
        )
    )
    val habits: StateFlow<List<Habit>> = _habits.asStateFlow()

    fun toggleHabit(habitId: Int) {
        _habits.update { currentList ->
            currentList.map { habit ->
                if (habit.id == habitId) habit.copy(isCompleted = !habit.isCompleted)
                else habit
            }
        }
    }

    fun addHabit(title: String, subtitle: String) {
        _habits.update { currentList ->
            val newId = (currentList.maxOfOrNull { it.id } ?: 0) + 1
            currentList + Habit(newId, title, subtitle)
        }
    }

    fun deleteHabit(habitId: Int) {
        _habits.update { currentList ->
            currentList.filter { it.id != habitId }
        }
    }
}