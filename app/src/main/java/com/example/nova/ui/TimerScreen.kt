package com.example.nova.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nova.timer.PomodoroPhase

@Composable
fun TimerScreen(viewModel: TimerViewModel = viewModel()) {
    val state by viewModel.timerState.collectAsState()
    val sessionsToday by viewModel.sessionsToday.collectAsState()
    val minutesToday by viewModel.minutesToday.collectAsState()

    val phaseLabel = when (state.phase) {
        PomodoroPhase.WORK -> "Study"
        PomodoroPhase.SHORT_BREAK -> "Short break"
        PomodoroPhase.LONG_BREAK -> "Long break"
    }
    val minutes = state.remainingSeconds / 60
    val seconds = state.remainingSeconds % 60
    val progress = if (state.totalSeconds > 0) {
        1f - (state.remainingSeconds.toFloat() / state.totalSeconds.toFloat())
    } else 0f

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(phaseLabel, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))

        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(220.dp),
                strokeWidth = 8.dp
            )
            Text(
                "%02d:%02d".format(minutes, seconds),
                style = MaterialTheme.typography.displayMedium
            )
        }

        Spacer(Modifier.height(8.dp))
        Text("Cycle ${state.cyclesCompleted + 1}", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            when {
                !state.hasStarted -> Button(onClick = { viewModel.start() }) { Text("Start") }
                state.isRunning -> Button(onClick = { viewModel.pause() }) { Text("Pause") }
                else -> Button(onClick = { viewModel.resume() }) { Text("Resume") }
            }
            if (state.hasStarted) {
                OutlinedButton(onClick = { viewModel.skip() }) { Text("Skip") }
                OutlinedButton(onClick = { viewModel.stop() }) { Text("Stop") }
            }
        }

        Spacer(Modifier.height(32.dp))
        Text(
            "Today: $sessionsToday sessions · $minutesToday min studied",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
