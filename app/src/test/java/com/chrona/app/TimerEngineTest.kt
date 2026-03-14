package com.chrona.app

import com.chrona.app.data.model.TimerPreset
import com.chrona.app.data.model.TimerStatus
import com.chrona.app.data.model.TimerType
import com.chrona.app.timer.TimerEngine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TimerEngineTest {

    private lateinit var engine: TimerEngine

    @Before
    fun setup() {
        engine = TimerEngine()
    }

    private fun countdownPreset(durationSec: Long = 60L) = TimerPreset(
        id = 1L,
        name = "Test",
        timerType = TimerType.COUNTDOWN,
        durationSeconds = durationSec,
        rounds = 1
    )

    private fun intervalPreset(workSec: Long = 20L, restSec: Long = 10L, rounds: Int = 3) = TimerPreset(
        id = 2L,
        name = "Interval",
        timerType = TimerType.INTERVAL,
        durationSeconds = 0L,
        rounds = rounds,
        workSeconds = workSec,
        restSeconds = restSec
    )

    @Test
    fun `initial state is IDLE`() {
        val state = engine.state.value
        assertEquals(TimerStatus.IDLE, state.status)
    }

    @Test
    fun `start sets status to RUNNING`() {
        engine.start(countdownPreset())
        assertEquals(TimerStatus.RUNNING, engine.state.value.status)
    }

    @Test
    fun `pause sets status to PAUSED`() {
        engine.start(countdownPreset())
        engine.pause()
        assertEquals(TimerStatus.PAUSED, engine.state.value.status)
    }

    @Test
    fun `resume sets status back to RUNNING`() {
        engine.start(countdownPreset())
        engine.pause()
        engine.resume()
        assertEquals(TimerStatus.RUNNING, engine.state.value.status)
    }

    @Test
    fun `stop resets state to idle`() {
        engine.start(countdownPreset())
        engine.stop()
        assertEquals(TimerStatus.IDLE, engine.state.value.status)
        assertEquals(0L, engine.state.value.remainingSeconds)
    }

    @Test
    fun `countdown preset sets remaining seconds correctly`() {
        val preset = countdownPreset(durationSec = 300L)
        engine.start(preset)
        assertEquals(300L, engine.state.value.remainingSeconds)
    }

    @Test
    fun `interval preset sets work seconds correctly`() {
        val preset = intervalPreset(workSec = 20L, restSec = 10L, rounds = 8)
        engine.start(preset)
        val state = engine.state.value
        assertEquals(20L, state.remainingSeconds)
        assertEquals(true, state.isWorkPhase)
        assertEquals(1, state.currentRound)
        assertEquals(8, state.totalRounds)
    }

    @Test
    fun `skipRound advances to rest phase for interval timer`() {
        val preset = intervalPreset(workSec = 20L, restSec = 10L, rounds = 3)
        engine.start(preset)
        assertTrue(engine.state.value.isWorkPhase)
        engine.skipRound()
        // After skipping work phase, should be in rest phase
        val state = engine.state.value
        assertFalse(state.isWorkPhase)
        assertEquals(10L, state.remainingSeconds)
    }

    @Test
    fun `skipRound advances round after rest phase`() {
        val preset = intervalPreset(workSec = 20L, restSec = 10L, rounds = 3)
        engine.start(preset)
        engine.skipRound() // skip work → rest
        engine.skipRound() // skip rest → next round work
        val state = engine.state.value
        assertEquals(2, state.currentRound)
        assertTrue(state.isWorkPhase)
    }

    @Test
    fun `finishing all rounds sets status to FINISHED`() {
        val preset = intervalPreset(workSec = 20L, restSec = 10L, rounds = 1)
        engine.start(preset)
        engine.skipRound() // skip work → rest
        engine.skipRound() // skip rest → finished (only 1 round)
        assertEquals(TimerStatus.FINISHED, engine.state.value.status)
    }

    @Test
    fun `reset restores preset initial state`() {
        val preset = countdownPreset(durationSec = 120L)
        engine.start(preset)
        engine.pause()
        engine.reset(preset)
        val state = engine.state.value
        assertEquals(TimerStatus.IDLE, state.status)
        assertEquals(120L, state.remainingSeconds)
    }

    @Test
    fun `progress fraction is 0 at start`() {
        engine.start(countdownPreset(durationSec = 60L))
        assertEquals(0f, engine.state.value.progressFraction)
    }

    @Test
    fun `timer type TABATA uses interval engine`() {
        val tabata = TimerPreset(
            id = 3L,
            name = "Tabata",
            timerType = TimerType.TABATA,
            durationSeconds = 0L,
            rounds = 8,
            workSeconds = 20L,
            restSeconds = 10L
        )
        engine.start(tabata)
        val state = engine.state.value
        assertEquals(TimerType.TABATA, state.timerType)
        assertEquals(8, state.totalRounds)
        assertEquals(20L, state.remainingSeconds)
    }

    @Test
    fun `EMOM timer runs as single-phase countdown with rounds`() {
        val emom = TimerPreset(
            id = 4L,
            name = "10 min EMOM",
            timerType = TimerType.EMOM,
            durationSeconds = 60L,  // 60s per round
            rounds = 10
        )
        engine.start(emom)
        val state = engine.state.value
        assertEquals(TimerType.EMOM, state.timerType)
        assertEquals(10, state.totalRounds)
        assertEquals(60L, state.remainingSeconds)
    }
}
