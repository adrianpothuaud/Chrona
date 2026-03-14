package com.chrona.app.data.repository

import com.chrona.app.data.db.dao.AlarmDao
import com.chrona.app.data.db.entity.AlarmEntity
import com.chrona.app.data.model.Alarm
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AlarmRepository @Inject constructor(
    private val dao: AlarmDao
) {
    fun getAllAlarms(): Flow<List<Alarm>> =
        dao.getAllAlarmsFlow().map { list -> list.map(AlarmEntity::toDomain) }

    suspend fun getAlarmById(id: Long): Alarm? = dao.getById(id)?.toDomain()

    suspend fun saveAlarm(alarm: Alarm): Long {
        val entity = AlarmEntity.fromDomain(alarm)
        return if (alarm.id == 0L) dao.insert(entity)
        else {
            dao.update(entity)
            alarm.id
        }
    }

    suspend fun deleteAlarm(alarm: Alarm) = dao.delete(AlarmEntity.fromDomain(alarm))

    suspend fun deleteAlarmById(id: Long) = dao.deleteById(id)

    suspend fun setAlarmEnabled(id: Long, enabled: Boolean) = dao.setEnabled(id, enabled)
}
