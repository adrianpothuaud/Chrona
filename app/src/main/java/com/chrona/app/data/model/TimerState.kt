package com.chrona.app.data.model

/**
 * Represents the current state of the timer engine.
 */
enum class TimerStatus {
    IDLE,
    RUNNING,
    PAUSED,
    FINISHED
}

data class TimerState(
    val status: TimerStatus = TimerStatus.IDLE,
    val presetId: Long = -1L,
    val presetName: String = "",
    val timerType: TimerType = TimerType.COUNTDOWN,
    val totalDurationSeconds: Long = 0L,
    val remainingSeconds: Long = 0L,
    val currentRound: Int = 0,
    val totalRounds: Int = 1,
    val isWorkPhase: Boolean = true,        // true = work, false = rest
    val workSeconds: Long = 0L,
    val restSeconds: Long = 0L
) {
    val progressFraction: Float
        get() = if (totalDurationSeconds == 0L) 0f
        else (totalDurationSeconds - remainingSeconds).toFloat() / totalDurationSeconds.toFloat()

    val currentPhaseDurationSeconds: Long
        get() = if (isWorkPhase) workSeconds.coerceAtLeast(totalDurationSeconds)
        else restSeconds

    val isRunning: Boolean get() = status == TimerStatus.RUNNING
    val isPaused: Boolean get() = status == TimerStatus.PAUSED
    val isFinished: Boolean get() = status == TimerStatus.FINISHED
    val isIdle: Boolean get() = status == TimerStatus.IDLE
}
