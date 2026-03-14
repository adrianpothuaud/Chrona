package com.chrona.app

import com.chrona.app.data.db.dao.TimerPresetDao
import com.chrona.app.data.db.entity.TimerPresetEntity
import com.chrona.app.data.model.TimerPreset
import com.chrona.app.data.model.TimerType
import com.chrona.app.data.repository.SortOrder
import com.chrona.app.data.repository.TimerRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*

class TimerRepositoryTest {

    private lateinit var dao: TimerPresetDao
    private lateinit var repository: TimerRepository

    private val sampleEntity = TimerPresetEntity(
        id = 1L,
        name = "Pasta Timer",
        timerType = TimerType.COUNTDOWN.name,
        durationSeconds = 420L,
        rounds = 1,
        workSeconds = 0L,
        restSeconds = 0L,
        notes = "",
        tags = "food,kitchen",
        isFavorite = false,
        createdAt = 1000L,
        updatedAt = 1000L
    )

    @Before
    fun setup() {
        dao = mock()
        repository = TimerRepository(dao)
    }

    @Test
    fun `getAllPresets returns mapped domain objects`() = runTest {
        whenever(dao.getAllPresetsFlow()).thenReturn(flowOf(listOf(sampleEntity)))

        val presets = repository.getAllPresets().first()

        assertEquals(1, presets.size)
        val preset = presets[0]
        assertEquals("Pasta Timer", preset.name)
        assertEquals(TimerType.COUNTDOWN, preset.timerType)
        assertEquals(420L, preset.durationSeconds)
        assertEquals(listOf("food", "kitchen"), preset.tagList)
    }

    @Test
    fun `searchPresets uses dao search`() = runTest {
        whenever(dao.searchPresetsFlow("pasta")).thenReturn(flowOf(listOf(sampleEntity)))

        val results = repository.searchPresets("pasta").first()

        assertEquals(1, results.size)
        verify(dao).searchPresetsFlow("pasta")
    }

    @Test
    fun `savePreset inserts new preset when id is 0`() = runTest {
        val newPreset = TimerPreset(
            id = 0L,
            name = "New Timer",
            timerType = TimerType.COUNTDOWN,
            durationSeconds = 60L
        )
        whenever(dao.insert(any())).thenReturn(42L)

        val id = repository.savePreset(newPreset)

        assertEquals(42L, id)
        verify(dao).insert(any())
        verify(dao, never()).update(any())
    }

    @Test
    fun `savePreset updates existing preset when id is non-zero`() = runTest {
        val existing = sampleEntity.toDomain()

        val id = repository.savePreset(existing)

        assertEquals(1L, id)
        verify(dao).update(any())
        verify(dao, never()).insert(any())
    }

    @Test
    fun `deletePreset calls dao delete`() = runTest {
        repository.deletePreset(sampleEntity.toDomain())
        verify(dao).delete(any())
    }

    @Test
    fun `toggleFavorite flips isFavorite`() = runTest {
        val preset = sampleEntity.toDomain().copy(isFavorite = false)
        repository.toggleFavorite(preset)
        verify(dao).setFavorite(1L, true)
    }

    @Test
    fun `getPresetsByType filters by timer type`() = runTest {
        whenever(dao.getPresetsByTypeFlow(TimerType.TABATA.name))
            .thenReturn(flowOf(emptyList()))

        repository.getPresetsByType(TimerType.TABATA).first()

        verify(dao).getPresetsByTypeFlow(TimerType.TABATA.name)
    }

    @Test
    fun `getAllPresets with NAME sort uses dao getAllPresetsByNameFlow`() = runTest {
        whenever(dao.getAllPresetsByNameFlow()).thenReturn(flowOf(emptyList()))

        repository.getAllPresets(SortOrder.NAME).first()

        verify(dao).getAllPresetsByNameFlow()
    }

    @Test
    fun `getAllPresets with DURATION sort uses dao getAllPresetsByDurationFlow`() = runTest {
        whenever(dao.getAllPresetsByDurationFlow()).thenReturn(flowOf(emptyList()))

        repository.getAllPresets(SortOrder.DURATION).first()

        verify(dao).getAllPresetsByDurationFlow()
    }

    @Test
    fun `TimerPreset tagList parses comma-separated tags`() {
        val preset = TimerPreset(
            id = 1L,
            name = "Test",
            timerType = TimerType.COUNTDOWN,
            durationSeconds = 60L,
            tags = "fitness, hiit, workout"
        )
        assertEquals(listOf("fitness", "hiit", "workout"), preset.tagList)
    }

    @Test
    fun `TimerPreset tagList is empty for blank tags`() {
        val preset = TimerPreset(
            id = 1L,
            name = "Test",
            timerType = TimerType.COUNTDOWN,
            durationSeconds = 60L,
            tags = ""
        )
        assertTrue(preset.tagList.isEmpty())
    }
}
