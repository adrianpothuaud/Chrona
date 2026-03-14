package com.chrona.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.chrona.app.data.model.TimerPreset
import com.chrona.app.data.model.TimerType

@Entity(tableName = "timer_presets")
data class TimerPresetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val timerType: String,
    val durationSeconds: Long,
    val rounds: Int,
    val workSeconds: Long,
    val restSeconds: Long,
    val notes: String,
    val tags: String,
    val isFavorite: Boolean,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): TimerPreset = TimerPreset(
        id = id,
        name = name,
        timerType = TimerType.valueOf(timerType),
        durationSeconds = durationSeconds,
        rounds = rounds,
        workSeconds = workSeconds,
        restSeconds = restSeconds,
        notes = notes,
        tags = tags,
        isFavorite = isFavorite,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(preset: TimerPreset): TimerPresetEntity = TimerPresetEntity(
            id = preset.id,
            name = preset.name,
            timerType = preset.timerType.name,
            durationSeconds = preset.durationSeconds,
            rounds = preset.rounds,
            workSeconds = preset.workSeconds,
            restSeconds = preset.restSeconds,
            notes = preset.notes,
            tags = preset.tags,
            isFavorite = preset.isFavorite,
            createdAt = preset.createdAt,
            updatedAt = preset.updatedAt
        )
    }
}
