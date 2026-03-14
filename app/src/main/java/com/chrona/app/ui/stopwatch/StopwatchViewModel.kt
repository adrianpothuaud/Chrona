package com.chrona.app.ui.stopwatch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class LapTime(
    val number: Int,
    val lapMillis: Long,
    val totalMillis: Long
)

data class StopwatchUiState(
    val isRunning: Boolean = false,
    val elapsedMillis: Long = 0L,
    val laps: List<LapTime> = emptyList()
) {
    val displayTime: String get() = formatMillis(elapsedMillis)
    val lapElapsed: Long
        get() = if (laps.isEmpty()) elapsedMillis
        else elapsedMillis - laps.last().totalMillis
}

@HiltViewModel
class StopwatchViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(StopwatchUiState())
    val uiState: StateFlow<StopwatchUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var startTime = 0L
    private var accumulatedMillis = 0L

    fun start() {
        if (_uiState.value.isRunning) return
        startTime = System.currentTimeMillis()
        _uiState.value = _uiState.value.copy(isRunning = true)
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(16L)  // ~60fps updates
                _uiState.value = _uiState.value.copy(
                    elapsedMillis = accumulatedMillis + (System.currentTimeMillis() - startTime)
                )
            }
        }
    }

    fun pause() {
        timerJob?.cancel()
        accumulatedMillis = _uiState.value.elapsedMillis
        _uiState.value = _uiState.value.copy(isRunning = false)
    }

    fun reset() {
        timerJob?.cancel()
        accumulatedMillis = 0L
        _uiState.value = StopwatchUiState()
    }

    fun lap() {
        val state = _uiState.value
        if (!state.isRunning) return
        val lapMillis = state.lapElapsed
        val newLap = LapTime(
            number = state.laps.size + 1,
            lapMillis = lapMillis,
            totalMillis = state.elapsedMillis
        )
        _uiState.value = state.copy(laps = state.laps + newLap)
    }
}

fun formatMillis(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val centiseconds = (millis % 1000) / 10
    return "%02d:%02d.%02d".format(minutes, seconds, centiseconds)
}
