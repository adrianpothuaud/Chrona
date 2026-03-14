package com.chrona.app.ui.alarm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chrona.app.data.model.Alarm
import com.chrona.app.data.repository.AlarmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlarmUiState(
    val alarms: List<Alarm> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val repository: AlarmRepository
) : ViewModel() {

    val uiState: StateFlow<AlarmUiState> = repository.getAllAlarms()
        .map { AlarmUiState(alarms = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AlarmUiState(isLoading = true)
        )

    fun toggleAlarm(alarm: Alarm) {
        viewModelScope.launch {
            repository.setAlarmEnabled(alarm.id, !alarm.isEnabled)
        }
    }

    fun deleteAlarm(alarm: Alarm) {
        viewModelScope.launch {
            repository.deleteAlarm(alarm)
        }
    }

    fun addAlarm(hour: Int, minute: Int, label: String = "") {
        viewModelScope.launch {
            repository.saveAlarm(Alarm(hour = hour, minute = minute, label = label))
        }
    }
}
