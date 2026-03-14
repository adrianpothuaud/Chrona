package com.chrona.app.ui.timer

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chrona.app.data.model.TimerPreset
import com.chrona.app.data.model.TimerType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerDetailScreen(
    timerId: Long,
    viewModel: TimerViewModel = hiltViewModel(),
    onEdit: () -> Unit,
    onStart: () -> Unit,
    onBack: () -> Unit
) {
    LaunchedEffect(timerId) { viewModel.loadPreset(timerId) }
    val preset by viewModel.selectedPreset.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(preset?.name ?: "Timer") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { padding ->
        preset?.let { p ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Type badge
                AssistChip(
                    onClick = {},
                    label = { Text(p.timerType.displayName) }
                )

                // Duration
                TimerDetailRow(
                    icon = Icons.Default.Timer,
                    label = "Duration",
                    value = formatDuration(p.durationSeconds)
                )

                // Rounds (for interval types)
                if (p.rounds > 1 || p.timerType != TimerType.COUNTDOWN) {
                    TimerDetailRow(
                        icon = Icons.Default.Repeat,
                        label = "Rounds",
                        value = "${p.rounds}"
                    )
                }

                // Work / Rest (for interval types)
                if (p.timerType in listOf(
                        TimerType.INTERVAL, TimerType.HIIT,
                        TimerType.TABATA, TimerType.MULTI_STAGE
                    )
                ) {
                    TimerDetailRow(
                        icon = Icons.Default.FitnessCenter,
                        label = "Work",
                        value = formatDuration(p.workSeconds)
                    )
                    TimerDetailRow(
                        icon = Icons.Default.Hotel,
                        label = "Rest",
                        value = formatDuration(p.restSeconds)
                    )
                }

                // Tags
                if (p.tagList.isNotEmpty()) {
                    TimerDetailRow(
                        icon = Icons.Default.Label,
                        label = "Tags",
                        value = p.tagList.joinToString(", ")
                    )
                }

                // Notes
                if (p.notes.isNotBlank()) {
                    TimerDetailRow(
                        icon = Icons.Default.Notes,
                        label = "Notes",
                        value = p.notes
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Start button
                Button(
                    onClick = onStart,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Timer", style = MaterialTheme.typography.titleMedium)
                }
            }
        } ?: Box(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Timer") },
            text = { Text("Are you sure you want to delete this timer?") },
            confirmButton = {
                TextButton(onClick = {
                    preset?.let { viewModel.deletePreset(it) }
                    onBack()
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun TimerDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon, contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
