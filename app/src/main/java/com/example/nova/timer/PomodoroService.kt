package com.example.nova.timer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.nova.data.AppDatabase
import com.example.nova.data.StudySessionRepository
import com.example.nova.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Keeps a Pomodoro-style study timer running even when the app isn't in the
 * foreground, showing a persistent notification with the live countdown and
 * Pause/Stop controls.
 */
class PomodoroService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob())
    private var tickerJob: Job? = null
    private lateinit var repository: StudySessionRepository

    override fun onCreate() {
        super.onCreate()
        repository = StudySessionRepository(AppDatabase.getInstance(applicationContext).studySessionDao())
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> handleStart()
            ACTION_PAUSE -> handlePause()
            ACTION_RESUME -> handleResume()
            ACTION_SKIP -> handleSkip()
            ACTION_STOP -> handleStop()
        }
        return START_NOT_STICKY
    }

    private fun handleStart() {
        val fresh = TimerState(
            phase = PomodoroPhase.WORK,
            remainingSeconds = PomodoroDefaults.WORK_MINUTES * 60,
            totalSeconds = PomodoroDefaults.WORK_MINUTES * 60,
            isRunning = true,
            hasStarted = true,
            cyclesCompleted = 0
        )
        _state.value = fresh
        val notification = buildNotification(fresh)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        startTicking()
    }

    private fun handlePause() {
        if (!_state.value.hasStarted) return
        _state.value = _state.value.copy(isRunning = false)
        updateNotification()
    }

    private fun handleResume() {
        val current = _state.value
        if (!current.hasStarted || current.remainingSeconds <= 0) return
        _state.value = current.copy(isRunning = true)
        updateNotification()
    }

    private fun handleSkip() {
        if (!_state.value.hasStarted) return
        onPhaseComplete(logIfWork = false)
    }

    private fun handleStop() {
        tickerJob?.cancel()
        _state.value = TimerState()
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun startTicking() {
        tickerJob?.cancel()
        tickerJob = serviceScope.launch {
            while (isActive) {
                delay(1000)
                val current = _state.value
                if (!current.isRunning) continue
                if (current.remainingSeconds > 1) {
                    _state.value = current.copy(remainingSeconds = current.remainingSeconds - 1)
                    updateNotification()
                } else {
                    onPhaseComplete(logIfWork = true)
                }
            }
        }
    }

    private fun onPhaseComplete(logIfWork: Boolean) {
        val current = _state.value
        if (current.phase == PomodoroPhase.WORK) {
            if (logIfWork) {
                serviceScope.launch {
                    repository.logSession(PomodoroDefaults.WORK_MINUTES, completed = true)
                }
            }
            val newCycles = current.cyclesCompleted + 1
            val goingLong = newCycles % PomodoroDefaults.CYCLES_BEFORE_LONG_BREAK == 0
            val nextPhase = if (goingLong) PomodoroPhase.LONG_BREAK else PomodoroPhase.SHORT_BREAK
            val nextSeconds = (if (goingLong) PomodoroDefaults.LONG_BREAK_MINUTES else PomodoroDefaults.SHORT_BREAK_MINUTES) * 60
            _state.value = current.copy(
                phase = nextPhase,
                remainingSeconds = nextSeconds,
                totalSeconds = nextSeconds,
                cyclesCompleted = newCycles,
                isRunning = true
            )
        } else {
            _state.value = current.copy(
                phase = PomodoroPhase.WORK,
                remainingSeconds = PomodoroDefaults.WORK_MINUTES * 60,
                totalSeconds = PomodoroDefaults.WORK_MINUTES * 60,
                isRunning = true
            )
        }
        vibrate()
        updateNotification()
    }

    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Study timer",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Shows the running study/break timer" }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun updateNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(_state.value))
    }

    private fun buildNotification(state: TimerState): Notification {
        val minutes = state.remainingSeconds / 60
        val seconds = state.remainingSeconds % 60
        val phaseLabel = when (state.phase) {
            PomodoroPhase.WORK -> "Studying"
            PomodoroPhase.SHORT_BREAK -> "Short break"
            PomodoroPhase.LONG_BREAK -> "Long break"
        }

        val contentIntent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleAction = if (state.isRunning) {
            NotificationCompat.Action(0, "Pause", actionPendingIntent(ACTION_PAUSE))
        } else {
            NotificationCompat.Action(0, "Resume", actionPendingIntent(ACTION_RESUME))
        }
        val stopAction = NotificationCompat.Action(0, "Stop", actionPendingIntent(ACTION_STOP))

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle("$phaseLabel · %02d:%02d".format(minutes, seconds))
            .setContentText("Cycle ${state.cyclesCompleted + 1} · tap to open")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent)
            .addAction(toggleAction)
            .addAction(stopAction)
            .build()
    }

    private fun actionPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, PomodoroService::class.java).apply { this.action = action }
        return PendingIntent.getService(
            this, action.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onDestroy() {
        tickerJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null

    companion object {
        const val CHANNEL_ID = "study_timer"
        const val NOTIFICATION_ID = 42
        const val ACTION_START = "com.example.nova.timer.START"
        const val ACTION_PAUSE = "com.example.nova.timer.PAUSE"
        const val ACTION_RESUME = "com.example.nova.timer.RESUME"
        const val ACTION_SKIP = "com.example.nova.timer.SKIP"
        const val ACTION_STOP = "com.example.nova.timer.STOP"

        private val _state = MutableStateFlow(TimerState())
        val state: StateFlow<TimerState> = _state
    }
}
