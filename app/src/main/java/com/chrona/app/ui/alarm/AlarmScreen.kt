package com.chrona.app.ui.alarm

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chrona.app.R
import com.chrona.app.data.model.Alarm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(
    viewModel: AlarmViewModel = hiltViewModel(),
    onNavigateToEdit: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_alarm)) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.action_add))
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.alarms.isEmpty()) {
                EmptyAlarmsState(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(uiState.alarms, key = { it.id }) { alarm ->
                        AlarmItem(
                            alarm = alarm,
                            onToggle = { viewModel.toggleAlarm(alarm) },
                            onDelete = { viewModel.deleteAlarm(alarm) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddAlarmDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { h, m, label ->
                viewModel.addAlarm(h, m, label)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AlarmItem(
    alarm: Alarm,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                text = alarm.timeString,
                style = MaterialTheme.typography.headlineMedium,
                color = if (alarm.isEnabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        },
        supportingContent = {
            Text(
                text = alarm.label.ifBlank { stringResource(R.string.alarm_label_default) },
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = alarm.isEnabled, onCheckedChange = { onToggle() })
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.action_delete))
                }
            }
        },
        modifier = Modifier.padding(horizontal = 8.dp)
    )
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
private fun EmptyAlarmsState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Alarm,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.empty_alarms),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AddAlarmDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int, Int, String) -> Unit
) {
    var hour by remember { mutableIntStateOf(8) }
    var minute by remember { mutableIntStateOf(0) }
    var label by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Alarm") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = "%02d".format(hour),
                        onValueChange = { hour = it.toIntOrNull()?.coerceIn(0, 23) ?: hour },
                        label = { Text("Hour") },
                        modifier = Modifier.weight(1f)
                    )
                    Text(":", style = MaterialTheme.typography.headlineMedium)
                    OutlinedTextField(
                        value = "%02d".format(minute),
                        onValueChange = { minute = it.toIntOrNull()?.coerceIn(0, 59) ?: minute },
                        label = { Text("Minute") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text(stringResource(R.string.field_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(hour, minute, label) }) {
                Text(stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}
