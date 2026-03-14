package com.chrona.app.data.model

/**
 * Domain model for a Timer Preset.
 *
 * Supports: Countdown, Interval, HIIT, TABATA, EMOM, AMRAP, Multi-Stage timers.
 */
data class TimerPreset(
    val id: Long = 0L,
    val name: String,
    val timerType: TimerType,
    /** Total duration in seconds (for COUNTDOWN, EMOM, AMRAP, etc.) */
    val durationSeconds: Long,
    /** Number of rounds (for HIIT, TABATA, INTERVAL, EMOM, MULTI_STAGE) */
    val rounds: Int = 1,
    /** Work phase duration in seconds (for interval-based timers) */
    val workSeconds: Long = 0L,
    /** Rest phase duration in seconds (for interval-based timers) */
    val restSeconds: Long = 0L,
    /** Optional notes / description */
    val notes: String = "",
    /** Comma-separated tags */
    val tags: String = "",
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val tagList: List<String>
        get() = if (tags.isBlank()) emptyList()
        else tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    val totalWorkSeconds: Long
        get() = when (timerType) {
            TimerType.COUNTDOWN, TimerType.EMOM, TimerType.AMRAP -> durationSeconds
            TimerType.INTERVAL, TimerType.HIIT, TimerType.TABATA, TimerType.MULTI_STAGE ->
                (workSeconds + restSeconds) * rounds
        }
}
