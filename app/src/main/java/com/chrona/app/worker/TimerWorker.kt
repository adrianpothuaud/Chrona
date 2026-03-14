package com.chrona.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.chrona.app.data.repository.TimerRepository
import com.chrona.app.timer.TimerEngine
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * WorkManager worker used to schedule and trigger timer actions.
 * This is useful for scheduling timers that should run in the background
 * even when the foreground service has not been started.
 */
@HiltWorker
class TimerWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val timerRepository: TimerRepository,
    private val timerEngine: TimerEngine
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_PRESET_ID = "preset_id"
        const val WORK_TAG = "chrona_timer"

        fun buildRequest(presetId: Long, delaySeconds: Long = 0): OneTimeWorkRequest =
            OneTimeWorkRequestBuilder<TimerWorker>()
                .setInputData(workDataOf(KEY_PRESET_ID to presetId))
                .addTag(WORK_TAG)
                .apply {
                    if (delaySeconds > 0) setInitialDelay(delaySeconds, TimeUnit.SECONDS)
                }
                .build()
    }

    override suspend fun doWork(): Result {
        val presetId = inputData.getLong(KEY_PRESET_ID, -1L)
        if (presetId < 0) return Result.failure()

        val preset = timerRepository.getPresetById(presetId) ?: return Result.failure()
        timerEngine.start(preset)
        return Result.success()
    }
}
