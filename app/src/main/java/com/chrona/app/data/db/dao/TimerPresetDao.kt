package com.chrona.app.data.db.dao

import androidx.room.*
import com.chrona.app.data.db.entity.TimerPresetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimerPresetDao {

    @Query("SELECT * FROM timer_presets ORDER BY updatedAt DESC")
    fun getAllPresetsFlow(): Flow<List<TimerPresetEntity>>

    @Query("SELECT * FROM timer_presets ORDER BY name ASC")
    fun getAllPresetsByNameFlow(): Flow<List<TimerPresetEntity>>

    @Query("SELECT * FROM timer_presets ORDER BY durationSeconds ASC")
    fun getAllPresetsByDurationFlow(): Flow<List<TimerPresetEntity>>

    @Query("SELECT * FROM timer_presets WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoritesFlow(): Flow<List<TimerPresetEntity>>

    @Query("SELECT * FROM timer_presets WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TimerPresetEntity?

    @Query("""
        SELECT * FROM timer_presets
        WHERE name LIKE '%' || :query || '%'
           OR tags LIKE '%' || :query || '%'
           OR notes LIKE '%' || :query || '%'
        ORDER BY updatedAt DESC
    """)
    fun searchPresetsFlow(query: String): Flow<List<TimerPresetEntity>>

    @Query("SELECT * FROM timer_presets WHERE timerType = :type ORDER BY updatedAt DESC")
    fun getPresetsByTypeFlow(type: String): Flow<List<TimerPresetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(preset: TimerPresetEntity): Long

    @Update
    suspend fun update(preset: TimerPresetEntity)

    @Delete
    suspend fun delete(preset: TimerPresetEntity)

    @Query("DELETE FROM timer_presets WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE timer_presets SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)
}
