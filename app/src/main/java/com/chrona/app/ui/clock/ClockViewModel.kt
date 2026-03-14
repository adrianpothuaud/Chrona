package com.chrona.app.ui.clock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class ClockUiState(
    val timeString: String = "",
    val dateString: String = "",
    val secondsAngle: Float = 0f,
    val minutesAngle: Float = 0f,
    val hoursAngle: Float = 0f
)

@HiltViewModel
class ClockViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ClockUiState())
    val uiState: StateFlow<ClockUiState> = _uiState.asStateFlow()

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss")
    private val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d")

    init {
        viewModelScope.launch {
            while (true) {
                val now = LocalDateTime.now()
                val h = now.hour % 12
                val m = now.minute
                val s = now.second
                _uiState.value = ClockUiState(
                    timeString = now.format(timeFormatter),
                    dateString = now.format(dateFormatter),
                    secondsAngle = s * 6f,
                    minutesAngle = m * 6f + s * 0.1f,
                    hoursAngle = h * 30f + m * 0.5f
                )
                delay(1_000L)
            }
        }
    }
}
