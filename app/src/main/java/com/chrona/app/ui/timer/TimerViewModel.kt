package com.chrona.app.ui.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.chrona.app.data.model.TimerPreset
import com.chrona.app.data.model.TimerState
import com.chrona.app.data.model.TimerType
import com.chrona.app.data.repository.SortOrder
import com.chrona.app.data.repository.TimerRepository
import com.chrona.app.timer.TimerEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TimerListUiState(
    val presets: List<TimerPreset> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val sortOrder: SortOrder = SortOrder.DATE_UPDATED,
    val filterType: TimerType? = null
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TimerViewModel @Inject constructor(
    private val repository: TimerRepository,
    val timerEngine: TimerEngine
) : ViewModel() {

    // ── List State ───────────────────────────────────────────────────────────

    private val _searchQuery = MutableStateFlow("")
    private val _sortOrder = MutableStateFlow(SortOrder.DATE_UPDATED)
    private val _filterType = MutableStateFlow<TimerType?>(null)

    val listUiState: StateFlow<TimerListUiState> = combine(
        _searchQuery,
        _sortOrder,
        _filterType
    ) { query, sort, filter ->
        Triple(query, sort, filter)
    }.flatMapLatest { (query, sort, filter) ->
        val flow = when {
            query.isNotBlank() -> repository.searchPresets(query)
            filter != null -> repository.getPresetsByType(filter)
            else -> repository.getAllPresets(sort)
        }
        flow.map { presets ->
            TimerListUiState(
                presets = presets,
                isLoading = false,
                searchQuery = query,
                sortOrder = sort,
                filterType = filter
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TimerListUiState(isLoading = true)
    )

    // ── Detail State ─────────────────────────────────────────────────────────

    private val _selectedPreset = MutableStateFlow<TimerPreset?>(null)
    val selectedPreset: StateFlow<TimerPreset?> = _selectedPreset.asStateFlow()

    // ── Timer Engine State ───────────────────────────────────────────────────

    val timerState: StateFlow<TimerState> = timerEngine.state

    // ── Actions ──────────────────────────────────────────────────────────────

    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setSortOrder(order: SortOrder) { _sortOrder.value = order }
    fun setFilterType(type: TimerType?) { _filterType.value = type }

    fun loadPreset(id: Long) {
        viewModelScope.launch {
            _selectedPreset.value = repository.getPresetById(id)
        }
    }

    fun savePreset(preset: TimerPreset, onSaved: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.savePreset(preset)
            onSaved(id)
        }
    }

    fun deletePreset(preset: TimerPreset) {
        viewModelScope.launch { repository.deletePreset(preset) }
    }

    fun toggleFavorite(preset: TimerPreset) {
        viewModelScope.launch { repository.toggleFavorite(preset) }
    }

    fun startTimer(preset: TimerPreset) {
        timerEngine.start(preset)
    }

    fun pauseTimer() { timerEngine.pause() }
    fun resumeTimer() { timerEngine.resume() }
    fun skipRound() { timerEngine.skipRound() }
    fun stopTimer() { timerEngine.stop() }
    fun resetTimer(preset: TimerPreset) { timerEngine.reset(preset) }
}
