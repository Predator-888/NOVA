package com.example.nova.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nova.timer.PomodoroPhase
import androidx.compose.ui.unit.sp

@Composable
fun TimerScreen(viewModel: TimerViewModel = viewModel()) {
    // 1. Maintain your exact state variables
    val state by viewModel.timerState.collectAsState()
    val sessionsToday by viewModel.sessionsToday.collectAsState()
    val minutesToday by viewModel.minutesToday.collectAsState()

    // 2. Map the phase label using your existing logic
    val phaseLabel = when (state.phase) {
        PomodoroPhase.WORK -> "Study"
        PomodoroPhase.SHORT_BREAK -> "Short break"
        PomodoroPhase.LONG_BREAK -> "Long break"
    }

    // 3. Calculate progress (draining the ring as time passes)
    val progress = if (state.totalSeconds > 0) {
        state.remainingSeconds.toFloat() / state.totalSeconds.toFloat()
    } else 1f

    // 4. Smoothly animate the ring
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1000, easing = LinearEasing),
        label = "timer_progress"
    )

    val minutes = state.remainingSeconds / 60
    val seconds = state.remainingSeconds % 60
    val timeString = "%02d:%02d".format(minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Phase and Cycle Info
        Text(
            text = phaseLabel.uppercase(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Cycle ${state.cyclesCompleted + 1}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // The Animated Timer Ring
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(280.dp)
        ) {
            // Background Track (faded)
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                strokeWidth = 12.dp,
                strokeCap = StrokeCap.Round,
            )

            // Foreground Animated Progress
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 12.dp,
                strokeCap = StrokeCap.Round,
            )

            // Large Time Display
            Text(
                text = timeString,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Sleek Controls using your original ViewModel functions
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            when {
                !state.hasStarted -> {
                    FloatingActionButton(
                        onClick = { viewModel.start() },
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primary,
                        elevation = FloatingActionButtonDefaults.elevation(8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Start", modifier = Modifier.size(32.dp))
                    }
                }
                state.isRunning -> {
                    FloatingActionButton(
                        onClick = { viewModel.pause() },
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = "Pause", modifier = Modifier.size(32.dp))
                    }
                }
                else -> {
                    FloatingActionButton(
                        onClick = { viewModel.resume() },
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primary
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Resume", modifier = Modifier.size(32.dp))
                    }
                }
            }

            if (state.hasStarted) {
                Spacer(modifier = Modifier.width(24.dp))

                // Stop Button
                FloatingActionButton(
                    onClick = { viewModel.stop() },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.size(48.dp) // Slightly smaller secondary buttons
                ) {
                    Icon(Icons.Default.Stop, contentDescription = "Stop", tint = MaterialTheme.colorScheme.onErrorContainer)
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Skip Button
                FloatingActionButton(
                    onClick = { viewModel.skip() },
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.FastForward, contentDescription = "Skip", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        // Daily Stats
        Text(
            text = "Today: $sessionsToday sessions • $minutesToday min studied",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}