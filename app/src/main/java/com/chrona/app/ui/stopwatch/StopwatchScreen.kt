package com.chrona.app.ui.stopwatch

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun StopwatchScreen(
    viewModel: StopwatchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Main timer display
        Text(
            text = uiState.displayTime,
            style = MaterialTheme.typography.displayLarge.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.primary
        )

        // Lap time display
        if (uiState.laps.isNotEmpty()) {
            Text(
                text = "Lap  ${formatMillis(uiState.lapElapsed)}",
                style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Controls
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lap / Reset button
            if (uiState.isRunning) {
                OutlinedButton(
                    onClick = viewModel::lap,
                    modifier = Modifier.size(72.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(Icons.Default.Flag, contentDescription = "Lap", modifier = Modifier.size(28.dp))
                }
            } else {
                OutlinedButton(
                    onClick = viewModel::reset,
                    modifier = Modifier.size(72.dp),
                    shape = MaterialTheme.shapes.extraLarge,
                    contentPadding = PaddingValues(0.dp),
                    enabled = uiState.elapsedMillis > 0
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(28.dp))
                }
            }

            // Start / Pause
            FloatingActionButton(
                onClick = {
                    if (uiState.isRunning) viewModel.pause() else viewModel.start()
                },
                modifier = Modifier.size(80.dp),
                containerColor = if (uiState.isRunning) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    if (uiState.isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (uiState.isRunning) "Pause" else "Start",
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Lap list
        if (uiState.laps.isNotEmpty()) {
            HorizontalDivider()
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(uiState.laps.reversed()) { lap ->
                    ListItem(
                        headlineContent = {
                            Text(
                                "Lap ${lap.number}",
                                style = MaterialTheme.typography.bodyLarge
                            )
                        },
                        trailingContent = {
                            Text(
                                formatMillis(lap.lapMillis),
                                style = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace)
                            )
                        },
                        supportingContent = {
                            Text(
                                "Total: ${formatMillis(lap.totalMillis)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        }
    }
}
