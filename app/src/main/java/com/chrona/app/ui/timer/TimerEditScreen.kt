package com.chrona.app.ui.timer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chrona.app.data.model.TimerPreset
import com.chrona.app.data.model.TimerType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerEditScreen(
    timerId: Long?,
    viewModel: TimerViewModel = hiltViewModel(),
    onSaved: (Long) -> Unit,
    onBack: () -> Unit
) {
    // Load existing preset if editing
    LaunchedEffect(timerId) {
        if (timerId != null && timerId > 0L) viewModel.loadPreset(timerId)
    }
    val existingPreset by viewModel.selectedPreset.collectAsStateWithLifecycle()

    // Form state
    var name by remember { mutableStateOf("") }
    var timerType by remember { mutableStateOf(TimerType.COUNTDOWN) }
    var durationMinutes by remember { mutableStateOf("5") }
    var rounds by remember { mutableStateOf("1") }
    var workSeconds by remember { mutableStateOf("20") }
    var restSeconds by remember { mutableStateOf("10") }
    var notes by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf("") }
    var isFavorite by remember { mutableStateOf(false) }
    var showTypeDropdown by remember { mutableStateOf(false) }

    // Pre-fill if editing
    LaunchedEffect(existingPreset) {
        existingPreset?.let { p ->
            if (timerId != null && timerId > 0L) {
                name = p.name
                timerType = p.timerType
                durationMinutes = (p.durationSeconds / 60).toString()
                rounds = p.rounds.toString()
                workSeconds = p.workSeconds.toString()
                restSeconds = p.restSeconds.toString()
                notes = p.notes
                tags = p.tags
                isFavorite = p.isFavorite
            }
        }
    }

    val isIntervalType = timerType in listOf(
        TimerType.INTERVAL, TimerType.HIIT, TimerType.TABATA, TimerType.MULTI_STAGE
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (timerId == null || timerId == 0L) "New Timer" else "Edit Timer") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val preset = buildPreset(
                                id = timerId ?: 0L,
                                existingPreset = existingPreset,
                                name = name,
                                timerType = timerType,
                                durationMinutes = durationMinutes,
                                rounds = rounds,
                                workSeconds = workSeconds,
                                restSeconds = restSeconds,
                                notes = notes,
                                tags = tags,
                                isFavorite = isFavorite
                            )
                            viewModel.savePreset(preset, onSaved)
                        },
                        enabled = name.isNotBlank()
                    ) {
                        Icon(Icons.Default.Check, "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Name
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Timer Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = name.isBlank()
            )

            // Timer Type
            Box {
                OutlinedTextField(
                    value = timerType.displayName,
                    onValueChange = {},
                    label = { Text("Timer Type") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showTypeDropdown = true }) {
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                    }
                )
                DropdownMenu(
                    expanded = showTypeDropdown,
                    onDismissRequest = { showTypeDropdown = false }
                ) {
                    TimerType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.displayName) },
                            onClick = { timerType = type; showTypeDropdown = false }
                        )
                    }
                }
            }

            // Duration (in minutes for simplicity)
            OutlinedTextField(
                value = durationMinutes,
                onValueChange = { durationMinutes = it },
                label = { Text("Duration (minutes)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            // Rounds
            OutlinedTextField(
                value = rounds,
                onValueChange = { rounds = it },
                label = { Text("Rounds") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            // Interval-specific fields
            if (isIntervalType) {
                OutlinedTextField(
                    value = workSeconds,
                    onValueChange = { workSeconds = it },
                    label = { Text("Work Duration (seconds)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                OutlinedTextField(
                    value = restSeconds,
                    onValueChange = { restSeconds = it },
                    label = { Text("Rest Duration (seconds)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            // Tags
            OutlinedTextField(
                value = tags,
                onValueChange = { tags = it },
                label = { Text("Tags (comma-separated)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6
            )

            // Favorite toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Add to Favorites", style = MaterialTheme.typography.bodyLarge)
                Switch(checked = isFavorite, onCheckedChange = { isFavorite = it })
            }

            // Timer type presets
            TimerTypePresetsRow(timerType = timerType) { selected ->
                timerType = selected.type
                workSeconds = selected.workSec.toString()
                restSeconds = selected.restSec.toString()
                rounds = selected.rounds.toString()
                durationMinutes = (selected.durationSec / 60).toString()
                name = selected.name
            }
        }
    }
}

// ── Quick presets ────────────────────────────────────────────────────────────

data class TimerQuickPreset(
    val name: String,
    val type: TimerType,
    val durationSec: Long,
    val rounds: Int,
    val workSec: Long,
    val restSec: Long
)

private val quickPresets = listOf(
    TimerQuickPreset("TABATA 8×", TimerType.TABATA, 0L, 8, 20L, 10L),
    TimerQuickPreset("30/30 HIIT", TimerType.HIIT, 0L, 10, 30L, 30L),
    TimerQuickPreset("10 min EMOM", TimerType.EMOM, 600L, 10, 0L, 0L),
    TimerQuickPreset("20 min AMRAP", TimerType.AMRAP, 1200L, 1, 0L, 0L),
    TimerQuickPreset("7 min Pasta", TimerType.COUNTDOWN, 420L, 1, 0L, 0L)
)

@Composable
private fun TimerTypePresetsRow(
    timerType: TimerType,
    onSelect: (TimerQuickPreset) -> Unit
) {
    Column {
        Text("Quick Presets", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            quickPresets.chunked(3).forEach { row ->
                row.forEach { preset ->
                    SuggestionChip(
                        onClick = { onSelect(preset) },
                        label = { Text(preset.name, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }
    }
}

private fun buildPreset(
    id: Long,
    existingPreset: TimerPreset?,
    name: String,
    timerType: TimerType,
    durationMinutes: String,
    rounds: String,
    workSeconds: String,
    restSeconds: String,
    notes: String,
    tags: String,
    isFavorite: Boolean
): TimerPreset {
    val durationSec = (durationMinutes.toLongOrNull() ?: 1L) * 60L
    val roundsInt = rounds.toIntOrNull()?.coerceAtLeast(1) ?: 1
    val workSec = workSeconds.toLongOrNull() ?: 20L
    val restSec = restSeconds.toLongOrNull() ?: 10L

    return TimerPreset(
        id = id,
        name = name.trim(),
        timerType = timerType,
        durationSeconds = durationSec,
        rounds = roundsInt,
        workSeconds = workSec,
        restSeconds = restSec,
        notes = notes.trim(),
        tags = tags.trim(),
        isFavorite = isFavorite,
        createdAt = existingPreset?.createdAt ?: System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
    )
}
