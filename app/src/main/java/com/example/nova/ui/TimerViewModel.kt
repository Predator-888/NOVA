package com.example.nova.ui

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nova.data.AppDatabase
import com.example.nova.data.StudySessionRepository
import com.example.nova.timer.PomodoroService
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.ZoneId

class TimerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StudySessionRepository(AppDatabase.getInstance(application).studySessionDao())
    private val appContext = application.applicationContext

    val timerState = PomodoroService.state

    private val startOfToday = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

    val sessionsToday = repository.getCompletedCountSince(startOfToday)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val minutesToday = repository.getTotalMinutesSince(startOfToday)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun start() = sendAction(PomodoroService.ACTION_START)
    fun pause() = sendAction(PomodoroService.ACTION_PAUSE)
    fun resume() = sendAction(PomodoroService.ACTION_RESUME)
    fun stop() = sendAction(PomodoroService.ACTION_STOP)
    fun skip() = sendAction(PomodoroService.ACTION_SKIP)

    private fun sendAction(action: String) {
        val intent = Intent(appContext, PomodoroService::class.java).apply { this.action = action }
        ContextCompat.startForegroundService(appContext, intent)
    }
}
