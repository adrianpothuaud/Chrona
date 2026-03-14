package com.chrona.app.ui.timer

import android.content.Context
import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chrona.app.data.model.TimerStatus
import com.chrona.app.timer.TimerService
import com.chrona.app.ui.theme.TimerPausedAmber
import com.chrona.app.ui.theme.TimerRestBlue
import com.chrona.app.ui.theme.TimerRunningGreen
import com.chrona.app.ui.theme.TimerWorkOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerRunningScreen(
    timerId: Long,
    viewModel: TimerViewModel = hiltViewModel(),
    onFinished: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    LaunchedEffect(timerId) {
        viewModel.loadPreset(timerId)
    }

    val preset by viewModel.selectedPreset.collectAsStateWithLifecycle()
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()

    // Start timer once preset is loaded and timer is idle
    LaunchedEffect(preset, timerState.isIdle) {
        val p = preset
        if (p != null && timerState.isIdle) {
            viewModel.startTimer(p)
            startTimerService(context)
        }
    }

    // Navigate away when finished
    LaunchedEffect(timerState.isFinished) {
        if (timerState.isFinished) onFinished()
    }

    val phaseColor by animateColorAsState(
        targetValue = when {
            timerState.isFinished -> MaterialTheme.colorScheme.primary
            timerState.isPaused -> TimerPausedAmber
            timerState.isWorkPhase -> TimerRunningGreen
            else -> TimerRestBlue
        },
        animationSpec = tween(500),
        label = "phase_color"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(timerState.presetName.ifBlank { "Timer" }) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.stopTimer()
                        stopTimerService(context)
                        onBack()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Stop & Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // Phase label
            Text(
                text = when {
                    timerState.totalRounds > 1 -> "Round ${timerState.currentRound} / ${timerState.totalRounds}"
                    else -> timerState.presetName
                },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (timerState.totalRounds > 1) {
                Text(
                    text = if (timerState.isWorkPhase) "WORK" else "REST",
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (timerState.isWorkPhase) TimerWorkOrange else TimerRestBlue
                )
            }

            // Circular progress + time
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { timerState.progressFraction },
                    modifier = Modifier.size(240.dp),
                    strokeWidth = 12.dp,
                    color = phaseColor,
                    trackColor = phaseColor.copy(alpha = 0.15f)
                )
                Text(
                    text = formatTime(timerState.remainingSeconds),
                    style = MaterialTheme.typography.displayMedium.copy(fontFamily = FontFamily.Monospace),
                    textAlign = TextAlign.Center
                )
            }

            // Status text
            Text(
                text = when (timerState.status) {
                    TimerStatus.RUNNING -> "Running"
                    TimerStatus.PAUSED -> "Paused"
                    TimerStatus.FINISHED -> "Finished!"
                    TimerStatus.IDLE -> "Starting..."
                },
                style = MaterialTheme.typography.titleMedium,
                color = phaseColor
            )

            // Controls
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skip Round
                if (timerState.totalRounds > 1) {
                    OutlinedButton(
                        onClick = viewModel::skipRound,
                        modifier = Modifier.size(60.dp),
                        shape = CircleShape,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Skip Round")
                    }
                }

                // Pause / Resume
                FloatingActionButton(
                    onClick = {
                        if (timerState.isRunning) viewModel.pauseTimer()
                        else if (timerState.isPaused) viewModel.resumeTimer()
                    },
                    modifier = Modifier.size(80.dp),
                    containerColor = phaseColor
                ) {
                    Icon(
                        if (timerState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (timerState.isRunning) "Pause" else "Resume",
                        modifier = Modifier.size(36.dp),
                        tint = Color.White
                    )
                }

                // Reset
                OutlinedButton(
                    onClick = {
                        preset?.let { viewModel.resetTimer(it) }
                    },
                    modifier = Modifier.size(60.dp),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset")
                }
            }
        }
    }
}

private fun startTimerService(context: Context) {
    val intent = Intent(context, TimerService::class.java)
    context.startForegroundService(intent)
}

private fun stopTimerService(context: Context) {
    context.stopService(Intent(context, TimerService::class.java))
}

private fun formatTime(seconds: Long): String {
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
