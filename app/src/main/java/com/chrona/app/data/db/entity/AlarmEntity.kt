package com.chrona.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.chrona.app.data.model.Alarm

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val hour: Int,
    val minute: Int,
    val label: String,
    val isEnabled: Boolean,
    val repeatDays: String,   // comma-separated int values
    val createdAt: Long
) {
    fun toDomain(): Alarm = Alarm(
        id = id,
        hour = hour,
        minute = minute,
        label = label,
        isEnabled = isEnabled,
        repeatDays = if (repeatDays.isBlank()) emptySet()
        else repeatDays.split(",").map { it.trim().toInt() }.toSet(),
        createdAt = createdAt
    )

    companion object {
        fun fromDomain(alarm: Alarm): AlarmEntity = AlarmEntity(
            id = alarm.id,
            hour = alarm.hour,
            minute = alarm.minute,
            label = alarm.label,
            isEnabled = alarm.isEnabled,
            repeatDays = alarm.repeatDays.joinToString(","),
            createdAt = alarm.createdAt
        )
    }
}
