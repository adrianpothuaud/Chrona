package com.chrona.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.chrona.app.data.db.dao.AlarmDao
import com.chrona.app.data.db.dao.TimerPresetDao
import com.chrona.app.data.db.entity.AlarmEntity
import com.chrona.app.data.db.entity.TimerPresetEntity

@Database(
    entities = [TimerPresetEntity::class, AlarmEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ChronaDatabase : RoomDatabase() {
    abstract fun timerPresetDao(): TimerPresetDao
    abstract fun alarmDao(): AlarmDao
}
