package com.chrona.app.data.repository

import com.chrona.app.data.db.dao.TimerPresetDao
import com.chrona.app.data.db.entity.TimerPresetEntity
import com.chrona.app.data.model.TimerPreset
import com.chrona.app.data.model.TimerType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

enum class SortOrder {
    DATE_UPDATED,
    NAME,
    DURATION,
    FAVORITES_FIRST
}

@Singleton
class TimerRepository @Inject constructor(
    private val dao: TimerPresetDao
) {
    fun getAllPresets(sortOrder: SortOrder = SortOrder.DATE_UPDATED): Flow<List<TimerPreset>> =
        when (sortOrder) {
            SortOrder.DATE_UPDATED -> dao.getAllPresetsFlow()
            SortOrder.NAME -> dao.getAllPresetsByNameFlow()
            SortOrder.DURATION -> dao.getAllPresetsByDurationFlow()
            SortOrder.FAVORITES_FIRST -> dao.getFavoritesFlow()
        }.map { list -> list.map(TimerPresetEntity::toDomain) }

    fun searchPresets(query: String): Flow<List<TimerPreset>> =
        dao.searchPresetsFlow(query).map { list -> list.map(TimerPresetEntity::toDomain) }

    fun getPresetsByType(type: TimerType): Flow<List<TimerPreset>> =
        dao.getPresetsByTypeFlow(type.name).map { list -> list.map(TimerPresetEntity::toDomain) }

    suspend fun getPresetById(id: Long): TimerPreset? =
        dao.getById(id)?.toDomain()

    suspend fun savePreset(preset: TimerPreset): Long {
        val entity = TimerPresetEntity.fromDomain(
            preset.copy(updatedAt = System.currentTimeMillis())
        )
        return if (preset.id == 0L) dao.insert(entity)
        else {
            dao.update(entity)
            preset.id
        }
    }

    suspend fun deletePreset(preset: TimerPreset) = dao.delete(TimerPresetEntity.fromDomain(preset))

    suspend fun deletePresetById(id: Long) = dao.deleteById(id)

    suspend fun toggleFavorite(preset: TimerPreset) =
        dao.setFavorite(preset.id, !preset.isFavorite)
}
