package com.example.nova.timer

object PomodoroDefaults {
    const val WORK_MINUTES = 25
    const val SHORT_BREAK_MINUTES = 5
    const val LONG_BREAK_MINUTES = 15
    const val CYCLES_BEFORE_LONG_BREAK = 4
}

enum class PomodoroPhase { WORK, SHORT_BREAK, LONG_BREAK }

data class TimerState(
    val phase: PomodoroPhase = PomodoroPhase.WORK,
    val remainingSeconds: Int = PomodoroDefaults.WORK_MINUTES * 60,
    val totalSeconds: Int = PomodoroDefaults.WORK_MINUTES * 60,
    val isRunning: Boolean = false,
    val hasStarted: Boolean = false,
    val cyclesCompleted: Int = 0
)
