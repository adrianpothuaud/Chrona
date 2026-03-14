package com.chrona.app.timer

import com.chrona.app.data.model.TimerPreset
import com.chrona.app.data.model.TimerState
import com.chrona.app.data.model.TimerStatus
import com.chrona.app.data.model.TimerType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Core timer engine that drives all timer types using coroutines.
 *
 * Supported types:
 *   - COUNTDOWN  – single duration countdown
 *   - EMOM       – every-minute-on-the-minute (same as countdown)
 *   - AMRAP      – as-many-rounds-as-possible (countdown)
 *   - INTERVAL   – alternating work/rest phases for N rounds
 *   - HIIT       – same as INTERVAL
 *   - TABATA     – 20s work / 10s rest × 8 rounds (configured as INTERVAL)
 *   - MULTI_STAGE– same engine as INTERVAL
 */
@Singleton
class TimerEngine @Inject constructor() {

    private val _state = MutableStateFlow(TimerState())
    val state: StateFlow<TimerState> = _state.asStateFlow()

    private var timerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // ── Public API ──────────────────────────────────────────────────────────

    fun start(preset: TimerPreset) {
        stop()
        val initial = buildInitialState(preset)
        _state.value = initial.copy(status = TimerStatus.RUNNING)
        timerJob = scope.launch { tick() }
    }

    fun pause() {
        if (_state.value.status == TimerStatus.RUNNING) {
            timerJob?.cancel()
            _state.value = _state.value.copy(status = TimerStatus.PAUSED)
        }
    }

    fun resume() {
        if (_state.value.status == TimerStatus.PAUSED) {
            _state.value = _state.value.copy(status = TimerStatus.RUNNING)
            timerJob = scope.launch { tick() }
        }
    }

    fun skipRound() {
        val s = _state.value
        if (!s.isRunning && !s.isPaused) return
        timerJob?.cancel()
        val next = advanceRound(s)
        _state.value = next
        if (next.status == TimerStatus.RUNNING) {
            timerJob = scope.launch { tick() }
        }
    }

    fun stop() {
        timerJob?.cancel()
        timerJob = null
        _state.value = TimerState()
    }

    fun reset(preset: TimerPreset) {
        stop()
        _state.value = buildInitialState(preset)
    }

    // ── Internal ────────────────────────────────────────────────────────────

    private suspend fun tick() {
        while (true) {
            delay(1_000L)
            val s = _state.value
            if (s.status != TimerStatus.RUNNING) break

            val remaining = s.remainingSeconds - 1
            if (remaining <= 0) {
                // Current phase/round done
                val next = advanceRound(s.copy(remainingSeconds = 0))
                _state.value = next
                if (next.status != TimerStatus.RUNNING) break
            } else {
                _state.value = s.copy(remainingSeconds = remaining)
            }
        }
    }

    private fun buildInitialState(preset: TimerPreset): TimerState {
        return when (preset.timerType) {
            TimerType.COUNTDOWN, TimerType.EMOM, TimerType.AMRAP -> TimerState(
                status = TimerStatus.IDLE,
                presetId = preset.id,
                presetName = preset.name,
                timerType = preset.timerType,
                totalDurationSeconds = preset.durationSeconds,
                remainingSeconds = preset.durationSeconds,
                currentRound = 1,
                totalRounds = preset.rounds,
                isWorkPhase = true,
                workSeconds = preset.durationSeconds,
                restSeconds = 0L
            )
            TimerType.INTERVAL, TimerType.HIIT, TimerType.TABATA, TimerType.MULTI_STAGE -> TimerState(
                status = TimerStatus.IDLE,
                presetId = preset.id,
                presetName = preset.name,
                timerType = preset.timerType,
                totalDurationSeconds = preset.workSeconds,
                remainingSeconds = preset.workSeconds,
                currentRound = 1,
                totalRounds = preset.rounds,
                isWorkPhase = true,
                workSeconds = preset.workSeconds,
                restSeconds = preset.restSeconds
            )
        }
    }

    /**
     * Called when [remainingSeconds] hits 0. Advances the phase/round or marks FINISHED.
     */
    private fun advanceRound(s: TimerState): TimerState {
        return when (s.timerType) {
            TimerType.COUNTDOWN, TimerType.EMOM, TimerType.AMRAP -> {
                // Single-phase – timer is done when round count exhausted
                val nextRound = s.currentRound + 1
                if (nextRound > s.totalRounds) {
                    s.copy(status = TimerStatus.FINISHED, remainingSeconds = 0)
                } else {
                    s.copy(
                        status = TimerStatus.RUNNING,
                        currentRound = nextRound,
                        remainingSeconds = s.workSeconds,
                        totalDurationSeconds = s.workSeconds
                    )
                }
            }
            TimerType.INTERVAL, TimerType.HIIT, TimerType.TABATA, TimerType.MULTI_STAGE -> {
                if (s.isWorkPhase) {
                    // Switch to rest (if any)
                    if (s.restSeconds > 0) {
                        s.copy(
                            isWorkPhase = false,
                            remainingSeconds = s.restSeconds,
                            totalDurationSeconds = s.restSeconds
                        )
                    } else {
                        // No rest → go to next round work
                        advanceToNextRound(s)
                    }
                } else {
                    // Rest finished → next round
                    advanceToNextRound(s)
                }
            }
        }
    }

    private fun advanceToNextRound(s: TimerState): TimerState {
        val nextRound = s.currentRound + 1
        return if (nextRound > s.totalRounds) {
            s.copy(status = TimerStatus.FINISHED, remainingSeconds = 0)
        } else {
            s.copy(
                status = TimerStatus.RUNNING,
                currentRound = nextRound,
                isWorkPhase = true,
                remainingSeconds = s.workSeconds,
                totalDurationSeconds = s.workSeconds
            )
        }
    }
}
